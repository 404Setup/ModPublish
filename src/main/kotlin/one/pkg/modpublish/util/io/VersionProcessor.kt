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
package one.pkg.modpublish.util.io

import com.google.gson.JsonArray
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import one.pkg.modpublish.api.API
import one.pkg.modpublish.api.NetworkUtil
import one.pkg.modpublish.util.io.FileAPI.getUserDataFile
import one.pkg.modpublish.util.io.JsonParser.fromJson
import one.pkg.modpublish.util.io.JsonParser.toJson
import one.pkg.modpublish.util.resources.LocalResources
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.math.min

@Suppress("UNUSED")
object VersionProcessor {
    private val LOG = Logger.getInstance(VersionProcessor::class.java)
    private const val MOJANG_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"
    private const val CURSEFORGE_URL = "https://api.curseforge.com/v1/minecraft/version"

    private fun fetchMinecraftVersions(): List<ProcessedVersion>? {
        return runCatching {
            LOG.info("Fetching Minecraft version data from Mojang API..")
            val request = API.baseRequestBuilder.url(MOJANG_URL).build()
            NetworkUtil.client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    LOG.error("Network request error: ${response.code}")
                    return null
                }
                val manifest = response.body.charStream().fromJson<MojangManifest>(MojangManifest::class.java)
                val processedVersions = manifest.versions.map { version ->
                    var releaseTime = version.releaseTime
                    if (releaseTime.endsWith("+00:00")) {
                        releaseTime = releaseTime.take(releaseTime.length - 6) + "Z"
                    }
                    ProcessedVersion(
                        version = version.id,
                        type = version.type,
                        id = -1,
                        date = releaseTime
                    )
                }

                LOG.info("Successfully processed ${processedVersions.size} versions")

                val latestInfo = manifest.latest
                LOG.info("Latest version info:")
                LOG.info("   Release: " + latestInfo["release"])
                LOG.info("   Snapshot: " + latestInfo["snapshot"])
                return processedVersions
            }
        }.onFailure {
            when (it) {
                is IOException -> LOG.error("Network request error", it)
                is JsonParseException -> LOG.error("JSON parsing error", it)
                else -> LOG.error("Unknown error", it)
            }
        }.getOrNull()
    }

    private fun fetchCurseforgeVersions(): List<CurseForgeVersion>? {
        return runCatching {
            LOG.info("Fetching version data from CurseForge API...")
            val request = API.baseRequestBuilder.url(CURSEFORGE_URL).build()
            NetworkUtil.client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    LOG.error("Network request error: ${response.code}")
                    return null
                }
                val data = response.body.charStream().fromJson<CurseForgeResponse>(CurseForgeResponse::class.java)
                val parsed = data.data ?: emptyList()

                LOG.info("Successfully got CurseForge API data with ${parsed.size} versions")
                return parsed
            }
        }.onFailure {
            when (it) {
                is IOException -> LOG.error("Network request error", it)
                is JsonParseException -> LOG.error("JSON parsing error", it)
                else -> LOG.error("Unknown error getting CurseForge data", it)
            }
        }.getOrNull()
    }

    private fun createVersionMapping(curseforgeVersions: List<CurseForgeVersion>): Map<String, Int> {
        val versionMapping = HashMap<String, Int>()
        for (version in curseforgeVersions) {
            val versionString = version.versionString
            val gameVersionId = version.gameVersionId
            if (!versionString.isNullOrEmpty() && gameVersionId != null) {
                versionMapping[versionString] = gameVersionId
            }
        }
        LOG.info("Created version mapping table with ${versionMapping.size} entries")
        return versionMapping
    }

    private fun mergeCurseforgeIds(
        minecraftVersions: List<ProcessedVersion>,
        versionMapping: Map<String, Int>
    ): List<ProcessedVersion> {
        val updatedVersions = ArrayList<ProcessedVersion>()
        var matchedCount = 0
        LOG.info("Merging version data...")

        for (version in minecraftVersions) {
            val gameVersionId = versionMapping[version.version] ?: -1

            val updated = version.copy(id = gameVersionId)

            if (gameVersionId != -1) {
                matchedCount++
            }

            updatedVersions.add(updated)
        }

        LOG.info("Version update complete: $matchedCount/${minecraftVersions.size} versions found matching game version IDs")
        return updatedVersions
    }

    private fun saveVersions(versions: List<ProcessedVersion>, outputFile: File): Boolean {
        return runCatching {
            if (outputFile.exists()) outputFile.delete()
            outputFile.createNewFile()
            outputFile.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                versions.toJson(writer)
                LOG.info("Successfully saved updated data to $outputFile")
                return true
            }
        }.onFailure { LOG.error("Error saving file, ", it) }.getOrDefault(false)
    }

    private fun saveVersions(versions: List<ProcessedVersion>, outputFile: String): Boolean {
        return saveVersions(versions, File(outputFile))
    }

    private fun showPreview(updatedVersions: List<ProcessedVersion>, count: Int) {
        LOG.info("Update Result Preview (First $count items):")
        LOG.info("------------------------------------------------------------")

        for (i in 0..<min(count, updatedVersions.size)) {
            val version = updatedVersions[i]
            val status = if (version.id != -1) "Matched" else "Unmatched"
            LOG.info("   " + (i + 1) + ". " + version.version + " (" + version.type + ") - ID: " + version.id + " - " + status)
        }

        if (updatedVersions.size > count) {
            LOG.info("   ... " + (updatedVersions.size - count) + " more versions")
        }

        val matchedCount = updatedVersions.count { it.id != -1 }
        val totalCount = updatedVersions.size
        val matchRate = if (totalCount > 0) (matchedCount * 100.0 / totalCount) else 0.0

        LOG.info("Match Statistics:")
        LOG.info("   Total Versions: $totalCount")
        LOG.info("   Matched Versions: $matchedCount")
        LOG.info("   Unmatched Versions: " + (totalCount - matchedCount))
        LOG.info("   Match Rate: " + String.format("%.1f", matchRate) + "%")
    }

    fun updateVersions(): Boolean {
        val mojang = fetchMinecraftVersions() ?: return false
        val curseforge = fetchCurseforgeVersions() ?: return false
        val mapping = createVersionMapping(curseforge)
        val merged = mergeCurseforgeIds(mojang, mapping)
        val saved = saveVersions(merged, "minecraft.version.json".getUserDataFile())
        if (saved) {
            LocalResources.clearMinecraftVersionsCache()
        }
        return saved
        //showPreview(merged, 5);
    }

    private data class MojangManifest(
        val latest: Map<String, String>,
        val versions: List<MojangVersion>
    )

    private data class MojangVersion(
        val id: String,
        val type: String,
        val releaseTime: String
    )

    private data class CurseForgeResponse(
        val data: List<CurseForgeVersion>?
    )

    private data class CurseForgeVersion(
        val versionString: String?,
        val gameVersionId: Int?
    )

    private data class ProcessedVersion(
        @SerializedName("v") val version: String,
        @SerializedName("t") val type: String,
        @SerializedName("i") val id: Int,
        @SerializedName("d") val date: String
    )
}
