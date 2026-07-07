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

import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import one.pkg.modpublish.api.NetworkUtil.client
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.PublishData
import one.pkg.modpublish.data.network.gitlab.GitlabData
import one.pkg.modpublish.data.result.BackResult
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.data.result.Result
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.util.io.GitInfo.getBrach
import one.pkg.modpublish.util.io.JsonParser.fromJson
import one.pkg.modpublish.util.io.JsonParser.toJson
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.io.IOException

@ApiStatus.Experimental
class GitlabAPI : GitPlatformAPI() {
    override val id: String = "GitLab"

    override suspend fun createVersion(data: PublishData, project: Project): PublishResult {
        return try {
            val tagName = if (data.versionNumber.startsWith("v")) data.versionNumber else "v${data.versionNumber}"
            var existingRelease = checkExistingRelease(tagName, project)

            if (existingRelease == null) {
                val result = createRelease(data, project)
                if (result is PublishResult) return result
                existingRelease = (result as BackResult).asString().fromJson()
            } else {
                updateReleaseDescription(tagName, data, project)
            }

            val existingAssets = getExistingAssetNames(existingRelease)

            val assetLinks = data.files.mapNotNull { file ->
                if (existingAssets.contains(file.name)) {
                    null
                } else {
                    uploadAssetToProject(file, project)
                }
            }

            if (assetLinks.isNotEmpty()) {
                linkAssetsToRelease(tagName, assetLinks, project)
            }

            PublishResult.EMPTY
        } catch (e: Exception) {
            PublishResult.create(this, "Failed to create GitLab release: ${e.message}")
        }
    }

    private fun HttpRequestBuilder.gitlab(project: Project) {
        header("PRIVATE-TOKEN", PID.GitlabToken.getProtect(project).data)
    }

    private suspend fun createRelease(data: PublishData, project: Project): Result = try {
        val repo = PID.GitlabRepo.get(project)
        val resp = client.post(RELEASES_URL.replace("{path}", repo)) {
            gitlab(project)
            json()
            setBody(createJsonBodyAsync(data, project))
        }

        resp.statusString()?.let { return PublishResult.create(this, "Failed to create release: $it") }
        BackResult.result(resp.bodyAsText())
    } catch (e: IOException) {
        PublishResult.create(this, "Network error: ${e.message}")
    }

    private suspend fun uploadAssetToProject(file: File, project: Project): String? = runCatching {
        val url = UPLOAD_URL.replace("{path}", PID.GitlabRepo.get(project))
        val resp = client.post(url) {
            gitlab(project)
            header("Content-Type", "application/octet-stream")
            setBody(file.readBytes())
        }

        if (resp.status.isSuccess()) {
            val response = resp.bodyAsText().fromJson().asJsonObject
            response.get("url")?.asString
        } else null
    }.getOrNull()

    private suspend fun linkAssetsToRelease(
        tagName: String,
        assetLinks: List<String>,
        project: Project
    ): PublishResult = try {
        assetLinks.forEach { url ->
            val fileName = url.substringAfterLast('/')
            val gitlabBaseUrl = PID.GitlabRepo.get(project).let { repo ->
                "https://gitlab.com/${repo.replace("%2F", "/")}"
            }
            val linkBody = JsonObject().apply {
                addProperty("name", fileName)
                addProperty("url", "$gitlabBaseUrl$url")
                addProperty("link_type", "package")
            }.toString()

            val reqUrl = "$RELEASES_URL/$tagName/assets/links".replace("{path}", PID.GitlabRepo.get(project))
            val resp = client.post(reqUrl) {
                gitlab(project)
                json()
                setBody(linkBody)
            }

            resp.statusString()?.let {
                return PublishResult.create(this, "Failed to link asset: $it")
            }
        }
        PublishResult.EMPTY
    } catch (e: IOException) {
        PublishResult.create(this, "Failed to link assets: ${e.message}")
    }

    private suspend fun checkExistingRelease(tagName: String, project: Project): JsonObject? = runCatching {
        val url = "$RELEASES_URL/$tagName".replace("{path}", PID.GitlabRepo.get(project))
        val resp = client.get(url) { gitlab(project) }
        if (resp.status.isSuccess()) resp.bodyAsText().fromJson() else null
    }.getOrNull()

    private fun getExistingAssetNames(release: JsonObject?): Set<String> {
        if (release == null) return emptySet()

        return runCatching {
            val assets = release.getAsJsonObject("assets")
                ?.getAsJsonArray("links")
                ?: return emptySet()

            if (assets.isEmpty) return emptySet()

            assets.mapNotNullTo(HashSet(assets.size())) { element ->
                element.asJsonObject.get("name")?.asString
            }
        }.getOrDefault(emptySet())
    }

    private suspend fun updateReleaseDescription(tagName: String, data: PublishData, project: Project): PublishResult =
        try {
            val updateBody = JsonObject().apply {
                addProperty("description", data.changelog)
            }.toString()

            val url = "$RELEASES_URL/$tagName".replace("{path}", PID.GitlabRepo.get(project))
            val resp = client.put(url) {
                gitlab(project)
                json()
                setBody(updateBody)
            }

            resp.statusString()?.let {
                return PublishResult.create(this, "Failed to update release: $it")
            }
            PublishResult.EMPTY
        } catch (e: IOException) {
            PublishResult.create(this, "Failed to update release: ${e.message}")
        }

    @Throws(IOException::class)
    override suspend fun getDefaultBranch(project: Project): String {
        val url = REPO_INFO_URL.replace("{path}", PID.GitlabRepo.get(project))
        val resp = client.get(url) { gitlab(project) }
        if (resp.status.isSuccess()) return resp.bodyAsText().fromJson().get("default_branch").asString
        return "main"
    }

    @Throws(IOException::class)
    override suspend fun getLatestCommitHash(branch: String, project: Project): String {
        val url = BRANCH_COMMIT_URL.replace("{path}", PID.GitlabRepo.get(project)).replace("{branch}", branch)
        val resp = client.get(url) { gitlab(project) }
        if (resp.status.isSuccess()) {
            val commit = resp.bodyAsText().fromJson().getAsJsonObject("commit")
            return commit.get("id").asString
        }
        return branch
    }

    override suspend fun getModInfo(modid: String, project: Project): ModInfo = ModInfo.empty()

    override fun createJsonBody(data: PublishData, project: Project): String {
        return ""
    }

    private suspend fun createJsonBodyAsync(data: PublishData, project: Project): String {
        val tagName = if (data.versionNumber.startsWith("v")) data.versionNumber else "v${data.versionNumber}"
        val branch = PID.GitlabBranch.get(project).ifEmpty { project.getBrach() }
        val ref = getTargetCommitish(branch, project)

        return GitlabData().apply {
            this.tagName = tagName
            this.ref = ref
            this.name = data.versionName
            this.description = data.changelog
            this.setReleaseChannel(data.releaseChannel)
        }.toJson().apply {
            LOG.info("now run $id publish: $this")
        }
    }

    companion object {
        private const val RELEASES_URL = "https://gitlab.com/api/v4/projects/{path}/releases"
        private const val REPO_INFO_URL = "https://gitlab.com/api/v4/projects/{path}"
        private const val BRANCH_COMMIT_URL = "https://gitlab.com/api/v4/projects/{path}/repository/branches/{branch}"
        private const val UPLOAD_URL = "https://gitlab.com/api/v4/projects/{path}/uploads"
        private val LOG = Logger.getInstance(GitlabAPI::class.java)
    }
}