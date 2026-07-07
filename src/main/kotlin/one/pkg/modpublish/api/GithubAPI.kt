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

class GithubAPI : GitPlatformAPI() {
    override val id: String = "Github"

    override suspend fun createVersion(data: PublishData, project: Project): PublishResult {
        return runCatching {
            val tagName = if (data.versionNumber.startsWith("v")) data.versionNumber else "v${data.versionNumber}"
            val existingRelease = checkExistingRelease(tagName, project)?.asJsonObject ?: run {
                val result = createRelease(data, project)
                if (result is PublishResult) return result
                (result as BackResult).asString().fromJson().asJsonObject
            }

            val uploadUrl =
                existingRelease.get("upload_url")?.asString?.split("{")?.first() ?: return PublishResult.create(
                    this,
                    "Failed to get upload URL"
                )
            data.files.fold(PublishResult.EMPTY) { acc, file ->
                if (!acc.isSuccess) acc else uploadAsset(file, project, uploadUrl)
            }
        }.getOrElse { PublishResult.create(this, "Failed to create GitHub release: ${it.message}") }
    }

    private fun HttpRequestBuilder.github(project: Project) {
        header("Accept", "application/vnd.github+json")
        header("X-GitHub-Api-Version", "2022-11-28")
        header("Authorization", "Bearer ${PID.GithubToken.getProtect(project).data}")
    }

    private suspend fun createRelease(data: PublishData, project: Project): Result = runCatching {
        val repo = PID.GithubRepo.get(project)
        val resp = client.post(RELEASES_URL.replace("{path}", repo)) {
            github(project)
            contentType(ContentType.Application.Json)
            //json()
            setBody(createJsonBodyAsync(data, project))
        }

        resp.statusString()?.let { return PublishResult.create(this, "Failed to create release: $it") }
        BackResult.result(resp.bodyAsText())
    }.getOrElse { PublishResult.create(this, "Network error: ${it.message}") }

    private suspend fun uploadAsset(file: File, project: Project, uploadUrl: String): PublishResult = runCatching {
        val assetUrl = "$uploadUrl?name=${file.name}"
        val resp = client.post(assetUrl) {
            github(project)
            header("Content-Type", "application/java-archive")
            setBody(file.readBytes())
        }

        resp.statusString()?.let { return PublishResult.create(this, "Failed to upload asset: $it") }
        PublishResult.EMPTY
    }.getOrElse { PublishResult.create(this, "Failed to upload asset: ${it.message}") }

    private suspend fun checkExistingRelease(tagName: String, project: Project): JsonObject? = runCatching {
        val url = "$RELEASES_URL/tags/$tagName".replace("{path}", PID.GithubRepo.get(project))
        val resp = client.get(url) { github(project) }
        if (resp.status.isSuccess()) resp.bodyAsText().fromJson() else null
    }.getOrNull()

    @Throws(IOException::class)
    override suspend fun getDefaultBranch(project: Project): String {
        val repo = PID.GithubRepo.get(project)
        val resp = client.get(REPO_INFO_URL.replace("{path}", repo)) { github(project) }
        if (resp.status.isSuccess()) return resp.bodyAsText().fromJson().get("default_branch").asString
        return "main"
    }

    @Throws(IOException::class)
    override suspend fun getLatestCommitHash(branch: String, project: Project): String {
        val repo = PID.GithubRepo.get(project)
        val url = BRANCH_COMMIT_URL.replace("{path}", repo).replace("{branch}", branch)
        val resp = client.get(url) { github(project) }
        if (resp.status.isSuccess()) {
            val commit = resp.bodyAsText().fromJson().getAsJsonObject("commit")
            return commit.get("sha").asString
        }
        return branch
    }

    override suspend fun getModInfo(modid: String, project: Project): ModInfo = ModInfo.empty()

    override fun createJsonBody(data: PublishData, project: Project): String {
        return ""
    }

    private suspend fun createJsonBodyAsync(data: PublishData, project: Project): String {
        val branch = PID.GithubBranch.get(project).ifEmpty { project.getBrach() }
        val targetCommitish = getTargetCommitish(branch, project)

        return GithubData().apply {
            this.tagName = if (data.versionNumber.startsWith("v")) data.versionNumber else "v${data.versionNumber}"
            this.targetCommitish = targetCommitish
            this.name = data.versionName
            this.body = data.changelog
            this.releaseChannel(data.releaseChannel)
            this.draft = false
            this.generateReleaseNotes = true
            this.makeLatest(true)
        }.toJson().apply {
            LOG.info("now run $id publish: $this")
        }
    }

    companion object {
        private const val RELEASES_URL = "https://api.github.com/repos/{path}/releases"
        private const val REPO_INFO_URL = "https://api.github.com/repos/{path}"
        private const val BRANCH_COMMIT_URL = "https://api.github.com/repos/{path}/branches/{branch}"
        private val LOG = Logger.getInstance(GithubAPI::class.java)
    }
}