/*
 * Copyright (C) 2025 - 2026 404Setup (https://github.com/404Setup)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package one.pkg.modpublish.api

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import one.pkg.modpublish.api.NetworkUtil.client
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.PublishData
import one.pkg.modpublish.data.internal.RequestStatus
import one.pkg.modpublish.data.network.modrinth.ModrinthData
import one.pkg.modpublish.data.network.modrinth.ModrinthDescription
import one.pkg.modpublish.data.network.modrinth.ProjectRelation
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.util.io.JsonParser.fromJson
import one.pkg.modpublish.util.io.JsonParser.toJson

class ModrinthAPI : API() {
    override val id: String = "Modrinth"

    private fun HttpRequestBuilder.modrinth(project: Project) {
        header("Authorization", PID.ModrinthToken.getProtect(project).data)
    }

    override suspend fun createVersion(data: PublishData, project: Project): PublishResult {
        return runCatching {
            val resp = client.post(URL + "version") {
                modrinth(project)
                setBody(
                    MultiPartFormDataContent(
                    formData {
                        append("data", createJsonBody(data, project))
                        data.files.forEachIndexed { i, file ->
                            val key = if (i == 0) "${file.name}-primary" else "${file.name}-${i - 1}"
                            append(key, file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "application/java-archive")
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                            })
                        }
                    }
                ))
            }
            resp.statusString()?.let { return PublishResult.create(this, it) }
            PublishResult.EMPTY
        }.getOrElse { PublishResult.create(this, it.message) }
    }

    override fun createJsonBody(data: PublishData, project: Project): String {
        return ModrinthData().apply {
            this.releaseChannel(data.releaseChannel)
            this.projectId = PID.ModrinthModID.get(project)
            this.versionBody = data.changelog
            this.status(RequestStatus.Listed)
            this.featured = true
            this.name = data.versionName
            this.versionNumber = data.versionNumber
            data.files.forEach { this.filePart(it) }
            data.loaders.forEach { this.loader(it) }
            data.minecraftVersions.forEach { this.gameVersion(it) }
            data.dependencies.forEach { d ->
                val info = d.modrinthModInfo
                if (!info?.modid.isNullOrBlank() && !info.slug.isNullOrBlank()) {
                    this.dependency(ProjectRelation.create(info.modid, d.type))
                }
            }
        }.toJson().apply {
            LOG.info("now run $id publish: $this")
        }
    }

    override suspend fun getModInfo(modid: String, project: Project): ModInfo {
        return runCatching {
            val resp = client.get(URL + "project/$modid") {
                modrinth(project)
                json()
            }
            resp.statusString()?.let { return ModInfo.of(it) }
            val obj = resp.bodyAsText().fromJson()
            ModInfo.of(modid, obj.get("title").asString, obj.get("slug").asString)
        }.getOrElse { ModInfo.of(it.message) }
    }

    override suspend fun patchDescription(
        modid: String,
        body: String,
        project: Project
    ): PublishResult {
        return runCatching {
            val resp = client.patch(URL + "project/$modid") {
                modrinth(project)
                json()
                setBody(ModrinthDescription.request(body))
            }
            resp.statusString()?.let { return PublishResult.create(this, it) }
            PublishResult.EMPTY
        }.getOrElse { PublishResult.create(this, it.message) }
    }

    companion object {
        private const val URL = "https://api.modrinth.com/v2/"
        private val LOG = Logger.getInstance(ModrinthAPI::class.java)
    }
}