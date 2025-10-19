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
import one.pkg.modpublish.data.internal.SideType
import one.pkg.modpublish.util.io.toml.KTomlArray
import one.pkg.modpublish.util.io.toml.KTomlParser
import java.io.InputStream
import java.lang.AutoCloseable

@Suppress("UNUSED")
data class ModTomlParser(val parser: KTomlParser) : AutoCloseable {
    fun get(): LocalModInfo? = runCatching {
        val mods = parser.getAsTomlParser("mods")?.takeUnless { it.isEmpty() } ?: run {
            LOG.warn("No mods section found in TOML file")
            return null
        }

        val deps = getArray(mods.getAsString("modId"))
        val clientSide = if (parser.getAsBoolean("clientSideOnly")) SideType.CLIENT else SideType.BOTH

        LocalModInfo(
            name = mods.getAsString("displayName"),
            version = mods.getAsString("version"),
            versionRange = deps?.let(::getMinecraftVersions).orEmpty(),
            sideType = if (clientSide == SideType.CLIENT) SideType.CLIENT else deps?.let(::getSideType) ?: SideType.BOTH
        )
    }.onFailure { LOG.error("Failed to parse mod.toml", it) }.getOrNull()

    private fun getSideType(arrays: KTomlArray): SideType =
        arrays.firstOrNull { it.getAsString("modId") in setOf("minecraft", "forge", "neoforge") }
            ?.getAsString("side")
            ?.let {
                when (it) {
                    "BOTH" -> SideType.BOTH
                    "CLIENT" -> SideType.CLIENT
                    else -> SideType.SERVER
                }
            } ?: SideType.BOTH

    private fun getArray(modId: String): KTomlArray? =
        parser.getAsTomlParser("dependencies")
            ?.getAsTomlArray(modId)
            ?.takeUnless { it.isEmpty }

    private fun getMinecraftVersions(arrays: KTomlArray): String =
        arrays.firstOrNull { it.getAsString("modId") == "minecraft" }
            ?.getAsString("versionRange")
            ?: ""

    override fun close() { /* no-op */
    }

    companion object {
        private val LOG = Logger.getInstance(ModTomlParser::class.java)

        fun InputStream.toModTomlParser(): ModTomlParser = ModTomlParser(KTomlParser.fromStream(this))
    }
}