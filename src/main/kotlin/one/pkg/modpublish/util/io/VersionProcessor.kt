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
package one.pkg.modpublish.util.io

import com.google.gson.JsonArray
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import one.pkg.modpublish.api.API
import one.pkg.modpublish.api.NetworkUtil
import one.pkg.modpublish.util.io.FileAPI.getUserDataFile
import one.pkg.modpublish.util.io.JsonParser.fromJson
import one.pkg.modpublish.util.io.JsonParser.toJson
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.math.min

object VersionProcessor {
    private val LOG = Logger.getInstance(VersionProcessor::class.java)
    private const val MOJANG_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"
    private const val CURSEFORGE_URL = "https://api.curseforge.com/v1/minecraft/version"

    private fun fetchMinecraftVersions(): MutableList<MutableMap<String, Any>>? {
        try {
            LOG.info("Fetching Minecraft version data from Mojang API..")
            val request = API.baseRequestBuilder.url(MOJANG_URL).build()
            NetworkUtil.client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    LOG.error("❌ Network request error: ${response.code}")
                    return null
                }
                val body = response.body.string()
                val data = body.fromJson().asJsonObject
                val processedVersions: MutableList<MutableMap<String, Any>> = arrayListOf()

                val versions = data.getAsJsonArray("versions")
                for (element in versions) {
                    val version = element.asJsonObject
                    var releaseTime = version.get("releaseTime").asString
                    if (releaseTime.endsWith("+00:00")) {
                        releaseTime = releaseTime.take(releaseTime.length - 6) + "Z"
                    }

                    val processedVersion: MutableMap<String, Any> = LinkedHashMap()
                    processedVersion["v"] = version.get("id").asString
                    processedVersion["t"] = version.get("type").asString
                    processedVersion["d"] = releaseTime
                    processedVersions.add(processedVersion)
                }

                LOG.info("✅ Successfully processed ${processedVersions.size} versions")

                val latestInfo = data.getAsJsonObject("latest")
                LOG.info("📋 Latest version info:")
                LOG.info("   Release: " + latestInfo.get("release").asString)
                LOG.info("   Snapshot: " + latestInfo.get("snapshot").asString)
                return processedVersions
            }
        } catch (e: IOException) {
            LOG.error("❌ Network request error", e)
        } catch (e: JsonParseException) {
            LOG.error("❌ JSON parsing error", e)
        } catch (e: Exception) {
            LOG.error("❌ Unknown error", e)
        }
        return null
    }

    private fun fetchCurseforgeVersions(): MutableList<MutableMap<String, Any>>? {
        try {
            LOG.info("Fetching version data from CurseForge API...")
            val request = API.baseRequestBuilder.url(CURSEFORGE_URL).build()
            NetworkUtil.client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    LOG.error("❌ Network request error: ${response.code}")
                    return null
                }
                val body = response.body.string()
                val data = body.fromJson().asJsonObject
                val versions = if (data.has("data")) data.getAsJsonArray("data") else JsonArray()

                val listType = object : TypeToken<MutableList<MutableMap<String, Any>>>() {
                }.type
                val parsed = versions.fromJson<MutableList<MutableMap<String, Any>>>(listType)

                LOG.info("✅ Successfully got CurseForge API data with ${parsed.size} versions")
                return parsed
            }
        } catch (e: IOException) {
            LOG.error("❌ Network request error", e)
        } catch (e: JsonParseException) {
            LOG.error("❌ JSON parsing error", e)
        } catch (e: Exception) {
            LOG.error("❌ Unknown error getting CurseForge data", e)
        }
        return null
    }

    private fun createVersionMapping(curseforgeVersions: MutableList<MutableMap<String, Any>>): MutableMap<String, Int> {
        val versionMapping: MutableMap<String, Int> = LinkedHashMap()
        for (version in curseforgeVersions) {
            val versionString = version.getOrDefault("versionString", "") as String
            val gameVersionId = version.getOrDefault("gameVersionId", -1) as Number
            if (!versionString.isEmpty()) {
                versionMapping[versionString] = gameVersionId.toInt()
            }
        }
        LOG.info("✅ Created version mapping table with ${versionMapping.size} entries")
        return versionMapping
    }

    private fun mergeCurseforgeIds(
        minecraftVersions: MutableList<MutableMap<String, Any>>,
        versionMapping: MutableMap<String, Int>
    ): MutableList<MutableMap<String, Any>> {
        val updatedVersions: MutableList<MutableMap<String, Any>> = ArrayList()
        var matchedCount = 0
        LOG.info("Merging version data...")

        for (version in minecraftVersions) {
            val versionString = version.getOrDefault("v", "") as String
            val gameVersionId: Int = versionMapping.getOrDefault(versionString, -1)

            val updated: MutableMap<String, Any> = LinkedHashMap()
            updated["v"] = versionString
            updated["t"] = version.getOrDefault("t", "")
            updated["i"] = gameVersionId
            updated["d"] = version.getOrDefault("d", "")

            if (gameVersionId != -1) {
                matchedCount++
            }

            updatedVersions.add(updated)
        }

        LOG.info("✅ Version update complete: $matchedCount/${minecraftVersions.size} versions found matching game version IDs")
        return updatedVersions
    }

    private fun saveVersions(versions: MutableList<MutableMap<String, Any>>, outputFile: File): Boolean {
        try {
            if (outputFile.exists()) outputFile.delete()
            outputFile.createNewFile()
            FileWriter(outputFile, StandardCharsets.UTF_8).use { writer ->
                versions.toJson(writer)
                LOG.info("✅ Successfully saved updated data to $outputFile")
                return true
            }
        } catch (e: IOException) {
            LOG.error("❌ Error saving file, ", e)
            return false
        }
    }

    private fun saveVersions(versions: MutableList<MutableMap<String, Any>>, outputFile: String): Boolean {
        return saveVersions(versions, File(outputFile))
    }

    private fun showPreview(updatedVersions: MutableList<MutableMap<String, Any>>, count: Int) {
        LOG.info("📊 Update Result Preview (First $count items):")
        LOG.info("------------------------------------------------------------")

        for (i in 0..<min(count, updatedVersions.size)) {
            val version = updatedVersions[i]
            val gameVersionId = (version["i"] as Number).toInt()
            val status = if (gameVersionId != -1) "✅ Matched" else "❌ Unmatched"
            LOG.info("   " + (i + 1) + ". " + version["v"] + " (" + version["t"] + ") - ID: " + gameVersionId + " - " + status)
        }

        if (updatedVersions.size > count) {
            LOG.info("   ... " + (updatedVersions.size - count) + " more versions")
        }

        val matchedCount =
            updatedVersions.stream().filter { v: MutableMap<String, Any> -> (v["i"] as Number).toInt() != -1 }
                .count()
        val totalCount = updatedVersions.size
        val matchRate = if (totalCount > 0) (matchedCount * 100.0 / totalCount) else 0.0

        LOG.info("📈 Match Statistics:")
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
        return saveVersions(merged, "minecraft.version.json".getUserDataFile())
        //showPreview(merged, 5);
    }
}
