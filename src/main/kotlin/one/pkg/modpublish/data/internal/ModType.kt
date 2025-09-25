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
package one.pkg.modpublish.data.internal

import com.intellij.openapi.vfs.VirtualFile
import one.pkg.modpublish.util.io.FileAPI
import one.pkg.modpublish.util.io.FileAPI.toFile
import one.pkg.modpublish.util.io.FileAPI.toJarFile
import one.pkg.modpublish.util.metadata.ModJsonParser
import one.pkg.modpublish.util.metadata.ModTomlParser
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.jar.JarFile
import java.util.zip.ZipEntry

enum class ModType(val fileName: String, val displayName: String, val curseForgeVersion: Int) {
    Fabric("fabric.mod.json", "Fabric", 7499) {
        override fun getMod(file: File): LocalModInfo? = getFabricMod(file)
    },
    Quilt("quilt.mod.json", "Quilt", 9153) {
        override fun getMod(file: File): LocalModInfo? = getFabricMod(file)
    },
    Forge("META-INF/mods.toml", "Forge", 7498) {
        override fun getMod(file: File): LocalModInfo? = getForgeMod(file)
    },
    NeoForge("META-INF/neoforge.mods.toml", "NeoForge", 10150) {
        override fun getMod(file: File): LocalModInfo? = getForgeMod(file)
    },
    Rift("riftmod.json", "Rift", 7500) {
        override fun getMod(file: File): LocalModInfo? = null // RiftMod version reading not supported
    };

    abstract fun getMod(file: File): LocalModInfo?

    fun getMod(file: VirtualFile): LocalModInfo? = getMod(file.toFile())

    fun getEntry(jar: JarFile): ZipEntry? = jar.getEntry(fileName)

    fun getStream(jar: JarFile): InputStream? = getEntry(jar)?.let { FileAPI.open(jar, it) }

    fun getID(): String = displayName.lowercase(Locale.ENGLISH)

    protected fun getFabricMod(file: File): LocalModInfo? = try {
        file.toJarFile().use { jar ->
            jar?.let { getStream(it) }.use { stream -> stream?.let { ModJsonParser(it) }?.get() }
        }
    } catch (_: Exception) {
        null
    }

    protected fun getForgeMod(file: File): LocalModInfo? = try {
        file.toJarFile().use { jar ->
            jar?.let { getStream(it) }.use { stream -> stream?.let { ModTomlParser.of(it) }?.get() }
        }
    } catch (_: Exception) {
        null
    }

    override fun toString(): String = displayName

    companion object {
        val valuesList = entries

        @JvmStatic
        fun of(file: File): ModType? = try {
            JarFile(file).use { jar -> of(jar) }
        } catch (_: Exception) {
            null
        }

        @JvmStatic
        fun of(jar: JarFile): ModType? = valuesList.firstOrNull { it.getEntry(jar) != null }

        @JvmStatic
        fun of(name: String): ModType? = valuesList.firstOrNull { it.displayName.equals(name, ignoreCase = true) }

        @JvmStatic
        fun getAll(file: File): List<ModType> = try {
            JarFile(file).use { jar -> valuesList.filter { it.getEntry(jar) != null } }
        } catch (_: Exception) {
            emptyList()
        }

        @JvmStatic
        fun getAll(file: VirtualFile): List<ModType> = getAll(file.toFile())
    }
}
