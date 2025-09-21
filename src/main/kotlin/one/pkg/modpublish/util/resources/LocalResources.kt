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
package one.pkg.modpublish.util.resources

import com.google.gson.reflect.TypeToken
import one.pkg.modpublish.data.local.MinecraftVersion
import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.data.local.LauncherInfo
import one.pkg.modpublish.data.local.SupportedInfo
import one.pkg.modpublish.util.io.FileAPI
import one.pkg.modpublish.util.io.JsonParser.fromJson
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.reflect.Type

object LocalResources {
    @JvmStatic
    val dpType: Type = object : TypeToken<List<DependencyInfo>>() {}.type
    private val mvType = object : TypeToken<List<MinecraftVersion>>() {}.type
    private val liType = object : TypeToken<List<LauncherInfo>>() {}.type

    @JvmStatic
    fun getSupportedInfo(): SupportedInfo = runCatching {
        LocalResources::class.java.getResourceAsStream("/META-INF/supported.info.json")?.use { stream ->
            InputStreamReader(stream).use { reader ->
                reader.fromJson(SupportedInfo::class.java)
            }
        } ?: throw Exception("supported.info.json not found")
    }.getOrElse { throw RuntimeException(it) }

    @JvmStatic
    fun getLauncherInfo(): List<LauncherInfo> = runCatching {
        LocalResources::class.java.getResourceAsStream("/META-INF/launcher.info.json")?.use { stream ->
            InputStreamReader(stream).use { reader ->
                reader.fromJson<List<LauncherInfo>>(liType)
            }
        } ?: throw Exception("launcher.info.json not found")
    }.getOrElse { throw RuntimeException(it) }

    @JvmStatic
    fun getMinecraftVersions(): List<MinecraftVersion> = runCatching {
        val localFile = FileAPI.getUserDataFile("minecraft.version.json")
        val stream = if (localFile.exists()) FileInputStream(localFile)
        else LocalResources::class.java.getResourceAsStream("/META-INF/minecraft.version.json")
        stream?.use {
            InputStreamReader(it).use { reader ->
                reader.fromJson<List<MinecraftVersion>>(mvType)
            }
        } ?: throw Exception("minecraft.version.json not found")
    }.getOrElse { throw RuntimeException(it) }
}
