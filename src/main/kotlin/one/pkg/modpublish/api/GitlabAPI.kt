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

import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import one.pkg.modpublish.api.NetworkUtil.client
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.PublishData
import one.pkg.modpublish.data.network.github.GithubData
import one.pkg.modpublish.data.result.BackResult
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.data.result.Result
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.util.io.GitInfo.getBrach
import one.pkg.modpublish.util.io.JsonParser.fromJson
import one.pkg.modpublish.util.io.JsonParser.toJson
import java.io.File
import java.io.IOException

class GitlabAPI : API() {
    override val id: String get() = "Gitlab"

    private var aBServer = false

    override fun getAB(): Boolean = aBServer
    override fun updateAB() {}

    fun getRequestBuilder(url: String, project: Project): Request.Builder =
        baseRequestBuilder.apply {
            header("Accept", "application/json")
            header("PRIVATE-TOKEN", PID.GitlabToken.getProtect(project).data)
            url(url.replace("{path}", PID.GithubRepo.get(project)))
        }

    override fun createJsonBody(data: PublishData, project: Project): String {
        val tagName = if (data.versionNumber.startsWith("v")) data.versionNumber else "v${data.versionNumber}"
        val branch = PID.GithubBranch.get(project).ifEmpty { project.getBrach() }
        val targetCommitish = getTargetCommitish(branch, project)

        return GithubData().apply {
            this.tagName = tagName
            this.targetCommitish = targetCommitish
            this.name = data.versionName
            this.body = data.changelog
            this.releaseChannel(data.releaseChannel)
            this.draft = false
            this.generateReleaseNotes = true
            this.makeLatest(true)
        }.toJson()
    }

    override fun createVersion(data: PublishData, project: Project): PublishResult = try {
        val tagName = if (data.versionNumber.startsWith("v")) data.versionNumber else "v${data.versionNumber}"
        var existingRelease = checkExistingRelease(tagName, project)?.asJsonObject
        if (existingRelease == null) {
            val result = createRelease(data, project)
            existingRelease = (result as? BackResult)?.asString()?.fromJson()?.asJsonObject
        }

        val uploadUrl = existingRelease?.get("assets")?.asJsonObject?.get("links")?.asJsonObject?.get("url")?.asString
            ?: return PublishResult.create(
                this,
                "Failed to get upload URL"
            )
        data.files.fold(PublishResult.EMPTY) { acc, file ->
            if (!acc.isSuccess) acc else uploadAsset(file, project, uploadUrl)
        }
    } catch (e: Exception) {
        PublishResult.create(this, "Failed to create GitLab release: ${e.message}")
    }

    private fun createRelease(data: PublishData, project: Project): Result = try {
        val request = getJsonRequest(getRequestBuilder(RELEASES_URL, project))
            .post(createJsonBody(data, project).toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { resp ->
            getStatus(resp)?.let { return PublishResult.create(this, "Failed to create release: $it") }
            BackResult.result(resp.body.string())
        }
    } catch (e: IOException) {
        PublishResult.create(this, "Network error: ${e.message}")
    }

    private fun uploadAsset(file: File, project: Project, uploadUrl: String): PublishResult = try {
        val fileBody = file.asRequestBody("application/java-archive".toMediaType())
        val request = baseRequestBuilder.url(uploadUrl)
            .header("PRIVATE-TOKEN", PID.GitlabToken.getProtect(project).data)
            .header("Content-Type", "application/java-archive")
            .post(fileBody)
            .build()

        client.newCall(request).execute().use { resp ->
            getStatus(resp)?.let { return PublishResult.create(this, "Failed to upload asset: $it") }
            PublishResult.EMPTY
        }
    } catch (e: IOException) {
        PublishResult.create(this, "Failed to upload asset: ${e.message}")
    }

    private fun checkExistingRelease(tagName: String, project: Project): JsonObject? = try {
        val url = "$RELEASES_URL/$tagName"
        val request = getRequestBuilder(url, project).get().build()
        client.newCall(request).execute()
            .use { resp -> if (resp.isSuccessful) resp.body.string().fromJson() else null }
    } catch (_: IOException) {
        null
    }

    private fun getTargetCommitish(branch: String, project: Project): String = try {
        branch.takeIf { it.isNotBlank() }?.let { getLatestCommitHash(it, project) } ?: getDefaultBranch(project)
    } catch (_: Exception) {
        branch.takeIf { it.isNotBlank() } ?: "main"
    }

    @Throws(IOException::class)
    private fun getDefaultBranch(project: Project): String {
        val request = getRequestBuilder(REPO_INFO_URL, project).get().build()
        client.newCall(request).execute().use { resp ->
            if (resp.isSuccessful) return resp.body.string().fromJson().get("default_branch").asString
        }
        return "main"
    }

    @Throws(IOException::class)
    private fun getLatestCommitHash(branch: String, project: Project): String {
        val url = BRANCH_COMMIT_URL.replace("{branch}", branch)
        val request = getRequestBuilder(url, project).get().build()
        client.newCall(request).execute().use { resp ->
            if (resp.isSuccessful) {
                val commit = resp.body.string().fromJson().getAsJsonObject("commit")
                return commit.get("id").asString
            }
        }
        return branch
    }

    override fun getModInfo(modid: String, project: Project): ModInfo = ModInfo.empty()

    companion object {
        private const val RELEASES_URL = "https://gitlab.com/api/v4/projects/{path}/releases"
        private const val REPO_INFO_URL = "https://gitlab.com/api/v4/projects/{path}"
        private const val BRANCH_COMMIT_URL = "https://gitlab.com/api/v4/projects/{path}/repository/branches/{branch}"
    }
}