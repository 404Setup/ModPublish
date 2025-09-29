/*
 * Copyright (C) 2025 404Setup (https://github.com/404Setup)
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

import com.intellij.openapi.project.Project
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import one.pkg.modpublish.api.NetworkUtil.client
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.PublishData
import one.pkg.modpublish.data.internal.RequestStatus
import one.pkg.modpublish.data.network.modrinth.ModrinthData
import one.pkg.modpublish.data.network.modrinth.ProjectRelation
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.util.io.JsonParser.getJsonObject
import one.pkg.modpublish.util.io.JsonParser.toJson
import java.io.IOException

class ModrinthAPI : API() {
    override val id: String get() = "Modrinth"

    private var aBServer = false

    override fun getAB(): Boolean = aBServer
    override fun updateAB() {}

    override fun createVersion(data: PublishData, project: Project): PublishResult {
        val requestBuilder = getFormRequest(getRequestBuilder("version", project))
        val body = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            addFormDataPart("data", createJsonBody(data, project))
            data.files.forEachIndexed { i, file ->
                val key = if (i == 0) "${file.name}-primary" else "${file.name}-${i - 1}"
                addFormDataPart(key, file.name, file.asRequestBody("application/java-archive".toMediaType()))
            }
        }.build()

        val request = requestBuilder.post(body).build()

        return try {
            client.newCall(request).execute().use { resp ->
                getStatus(resp)?.let { return PublishResult.create(this, it) }
                PublishResult.EMPTY
            }
        } catch (e: IOException) {
            PublishResult.create(this, e.message)
        }
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
        }.toJson()
    }

    override fun getModInfo(modid: String, project: Project): ModInfo {
        val request = getJsonRequest(getRequestBuilder("project/$modid", project)).get().build()
        return try {
            client.newCall(request).execute().use { resp ->
                getStatus(resp)?.let { return ModInfo.of(it) }
                val obj = resp.body.byteStream().getJsonObject()
                ModInfo.of(modid, obj.get("title").asString, obj.get("slug").asString)
            }
        } catch (e: IOException) {
            ModInfo.of(e.message)
        }
    }

    fun getRequestBuilder(url: String, project: Project): Request.Builder =
        baseRequestBuilder.header("Authorization", PID.ModrinthToken.getProtect(project).data)
            .url(URL + url)

    companion object {
        private const val URL = "https://api.modrinth.com/v2/"
    }
}