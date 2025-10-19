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
package one.pkg.modpublish.util.metadata

import com.intellij.openapi.diagnostic.Logger
import one.pkg.modpublish.data.internal.LocalModInfo
import one.pkg.modpublish.util.io.toml.KTomlParser
import java.io.InputStream
import java.lang.AutoCloseable

@Suppress("UNUSED")
data class ModTomlParser(val parser: KTomlParser) : AutoCloseable {
    fun get(): LocalModInfo? {
        return runCatching {
            val mods = parser.getAsTomlParser("mods")
            if (mods == null || mods.isEmpty()) {
                LOG.warn("No mods section found in TOML file")
                return null
            }

            val displayName = mods.getAsString("displayName")
            val version = mods.getAsString("version")

            LocalModInfo(displayName, version, getMinecraftVersions(mods.getAsString("modId")))
        }.onFailure { LOG.error("Failed to parse mod.toml", it) }.getOrNull()
    }

    fun getMinecraftVersions(modId: String): String {
        val dependencies = parser.getAsTomlParser("dependencies")
        if (dependencies != null) {
            val arrays = dependencies.getAsTomlArray(modId)
            if (arrays.isEmpty) return ""
            for (array in arrays)
                if (array.getAsString("modId") == "minecraft") return array.getAsString("versionRange")
        }

        return ""
    }

    override fun close() {
        // NONE
    }

    companion object {
        private val LOG = Logger.getInstance(ModTomlParser::class.java)

        fun InputStream.toModTomlParser(): ModTomlParser {
            return ModTomlParser(KTomlParser.fromStream(this))
        }
    }
}