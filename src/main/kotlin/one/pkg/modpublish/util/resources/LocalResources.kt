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
import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.data.local.MinecraftVersion
import one.pkg.modpublish.data.local.SupportedInfo
import one.pkg.modpublish.exception.ResourcesNotFoundException
import one.pkg.modpublish.util.io.FileAPI.getUserDataFile
import one.pkg.modpublish.util.io.JsonParser.fromJson
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.lang.reflect.Type

object LocalResources {
    val dpType: Type = object : TypeToken<List<DependencyInfo>>() {}.type
    private val mvType = object : TypeToken<List<MinecraftVersion>>() {}.type

    @Throws(
        ResourcesNotFoundException::class,
        FileNotFoundException::class,
        SecurityException::class,
        NullPointerException::class
    )
    fun getSupportedInfo(): SupportedInfo =
        LocalResources.javaClass.getResourceAsStream("/META-INF/supported.info.json")?.use { stream ->
            InputStreamReader(stream).use { reader ->
                reader.fromJson(SupportedInfo::class.java)
            }
        } ?: throw ResourcesNotFoundException("supported.info.json not found")

    @Throws(
        ResourcesNotFoundException::class,
        FileNotFoundException::class,
        SecurityException::class,
        NullPointerException::class
    )
    fun getMinecraftVersions(): List<MinecraftVersion> {
        val localFile = "minecraft.version.json".getUserDataFile()
        val stream = if (localFile.exists()) FileInputStream(localFile)
        else LocalResources.javaClass.getResourceAsStream("/META-INF/minecraft.version.json")
        return stream?.use {
            InputStreamReader(it).use { reader ->
                reader.fromJson<List<MinecraftVersion>>(mvType)
            }
        } ?: throw ResourcesNotFoundException("minecraft.version.json not found")
    }
}
