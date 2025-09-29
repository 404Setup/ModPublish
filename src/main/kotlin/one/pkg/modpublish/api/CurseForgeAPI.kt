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
import one.pkg.modpublish.data.network.curseforge.CurseForgeData
import one.pkg.modpublish.data.network.curseforge.CurseForgePublishResult
import one.pkg.modpublish.data.network.curseforge.ProjectRelation
import one.pkg.modpublish.data.result.BackResult
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.data.result.Result
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.util.io.JsonParser.fromJson
import one.pkg.modpublish.util.io.JsonParser.getJsonObject
import one.pkg.modpublish.util.io.JsonParser.toJson
import java.io.File
import java.io.IOException

class CurseForgeAPI : API() {
    private var aBServer = false

    override val id: String
        get() = "CurseForge"

    override fun getAB(): Boolean = aBServer

    override fun updateAB() {
        aBServer = !aBServer
    }

    private fun create(data: PublishData, project: Project, file: File, bResult: BackResult?): Result {
        val modid = PID.CurseForgeModID.get(project)
        val requestBuilder = getFormRequest(getRequestBuilder("projects/$modid/upload-file", project))
        val fileBody = file.asRequestBody("application/java-archive".toMediaType())

        val body = MultipartBody.Builder().apply {
            setType(MultipartBody.FORM)
            addFormDataPart("file", file.name, fileBody)
            addFormDataPart("metadata", createJsonBody(data, bResult))
        }.build()

        val request = requestBuilder.post(body).build()

        return try {
            client.newCall(request).execute().use { resp ->
                getStatus(resp)?.let { return PublishResult.create(this, it) }
                val result = resp.body.string().fromJson(CurseForgePublishResult::class.java)
                if (result.isSuccess) BackResult.result(result) else PublishResult.create(resp.body.string())
            }
        } catch (e: IOException) {
            PublishResult.create(this, e.message)
        }
    }

    override fun createVersion(data: PublishData, project: Project): PublishResult {
        if (aBServer) aBServer = false
        var bResult: BackResult? = null
        data.files.forEachIndexed { index, file ->
            when (val result = create(data, project, file, bResult)) {
                is BackResult -> if (index == 0) bResult = result
                is PublishResult -> if (result.isFailure) return result
            }
        }
        return PublishResult.EMPTY
    }

    override fun createJsonBody(data: PublishData, project: Project): String =
        createJsonBody(data, null)

    fun createJsonBody(data: PublishData, bResult: BackResult?): String {
        return CurseForgeData().apply {
            this.releaseType(data.releaseChannel)
            this.markdownChangelog(data.changelog)
            this.displayName = data.versionName
            bResult?.let { this.parentFileID = it.asCurseForgePublishResult().id } ?: run {
                data.minecraftVersions.forEach { gameVersion(it) }
                if (data.supportedInfo.client.enabled) this.gameVersion(data.supportedInfo.client.cfid)
                if (data.supportedInfo.server.enabled) this.gameVersion(data.supportedInfo.server.cfid)
                data.loaders.filter { it.curseForgeVersion > 0 }.forEach { this.gameVersion(it.curseForgeVersion) }
            }
            data.dependencies.forEach { dep ->
                dep.curseforgeModInfo?.let { info ->
                    if (!info.modid.isNullOrBlank() && !info.slug.isNullOrBlank()) {
                        this.dependency(ProjectRelation.create(info.slug, info.modid.toInt(), dep.type))
                    }
                }
            }
        }.toJson()
    }

    override fun getModInfo(modid: String, project: Project): ModInfo {
        if (!aBServer) aBServer = true
        val request = getJsonRequest(getRequestBuilder("mods/$modid", project)).get().build()
        return try {
            client.newCall(request).execute().use { resp ->
                getStatus(resp)?.let { return ModInfo.of(it) }
                val data = resp.body.byteStream().getJsonObject().getAsJsonObject("data")
                ModInfo.of(modid, data.get("name").asString, data.get("slug").asString)
            }
        } catch (e: IOException) {
            ModInfo.of(e.message)
        }
    }

    fun getRequestBuilder(url: String, project: Project): Request.Builder =
        baseRequestBuilder.apply {
            if (aBServer) {
                header("x-api-key", PID.CurseForgeStudioToken.getProtect(project).data)
                url(B_URL + url)
            } else {
                header("X-Api-Token", PID.CurseForgeToken.getProtect(project).data)
                url(A_URL + url)
            }
        }

    companion object {
        private const val A_URL = "https://minecraft.curseforge.com/api/"
        private const val B_URL = "https://api.curseforge.com/v1/"
    }
}
