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

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import one.pkg.modpublish.util.io.FileAPI.isJavaAgent
import one.pkg.modpublish.util.io.FileAPI.open
import one.pkg.modpublish.util.io.FileAPI.toFile
import one.pkg.modpublish.util.io.FileAPI.toJarFile
import one.pkg.modpublish.util.metadata.ModJsonParser
import one.pkg.modpublish.util.metadata.ModTomlParser.Companion.toModTomlParser
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipEntry

enum class ModType(val fileName: String, val displayName: String, val curseForgeVersion: Int) {
    Fabric("fabric.mod.json", "Fabric", 7499) {
        override fun getMod(file: File): LocalModInfo? = try {
            file.toJarFile().use { jar -> jar?.let { getFabricMod(it) } }
        } catch (e: Exception) {
            LOG.error(e)
            null
        }
    },
    Quilt("quilt.mod.json", "Quilt", 9153) {
        override fun getMod(file: File): LocalModInfo? = try {
            file.toJarFile().use { jar -> jar?.let { getFabricMod(it) } }
        } catch (e: Exception) {
            LOG.error(e)
            null
        }
    },
    Forge("META-INF/mods.toml", "Forge", 7498) {
        override fun getMod(file: File): LocalModInfo? = try {
            file.toJarFile().use { jar -> jar?.let { getForgeMod(it) } }
        } catch (e: Exception) {
            LOG.error(e)
            null
        }
    },
    NeoForge("META-INF/neoforge.mods.toml", "NeoForge", 10150) {
        override fun getMod(file: File): LocalModInfo? = try {
            file.toJarFile().use { jar -> jar?.let { getForgeMod(it) } }
        } catch (e: Exception) {
            LOG.error(e)
            null
        }
    },
    Rift("riftmod.json", "Rift", 7500) {
        override fun getMod(file: File): LocalModInfo? = try {
            file.toJarFile().use { jar ->
                jar?.let {
                    try {
                        getMCMod(it)
                    } catch (e: Exception) {
                        LOG.error(e)
                        getRiftMod(it)
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error(e)
            null
        }
    },
    LiteLoader("litemod.json", "LiteLoader", -1) {
        override fun getMod(file: File): LocalModInfo? = try {
            file.toJarFile().use { jar ->
                jar?.let {
                    try {
                        getMCMod(it)
                    } catch (e: Exception) {
                        LOG.error(e)
                        getLiteMod(it)
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error(e)
            null
        }
    },
    JavaAgent("", "JavaAgent", -1) {
        override fun getMod(file: File): LocalModInfo? = if (file.isJavaAgent()) {
            LocalModInfo(
                name = file.nameWithoutExtension,
                version = "unknown",
                versionRange = "1.0.0"
            )
        } else null

        override fun getID(): String = "java-agent"
    };

    abstract fun getMod(file: File): LocalModInfo?

    fun getMod(file: VirtualFile): LocalModInfo? = getMod(file.toFile())

    fun getEntry(jar: JarFile): ZipEntry? = jar.getEntry(fileName)

    fun getStream(jar: JarFile): InputStream? = getEntry(jar)?.let { jar.open(it) }

    open fun getID(): String = displayName.lowercase(Locale.ENGLISH)

    protected fun getFabricMod(file: JarFile): LocalModInfo? = try {
        file.use { jar ->
            getStream(jar).use { stream -> stream?.let { ModJsonParser(it) }?.getFabric() }
        }
    } catch (e: Exception) {
        LOG.error(e)
        null
    }

    protected fun getForgeMod(file: JarFile): LocalModInfo? = try {
        file.use { jar ->
            getStream(jar).use { stream -> stream?.toModTomlParser()?.get() }
        }
    } catch (e: Exception) {
        LOG.error(e)
        null
    }

    protected fun getLiteMod(file: JarFile): LocalModInfo? = try {
        file.use { jar ->
            getStream(jar).use { stream -> stream?.let { ModJsonParser(it) }?.getLiteMod() }
        }
    } catch (e: Exception) {
        LOG.error(e)
        null
    }

    protected fun getRiftMod(file: JarFile): LocalModInfo? = try {
        file.use { jar ->
            getStream(jar).use { stream -> stream?.let { ModJsonParser(it) }?.getRiftMod() }
        }
    } catch (e: Exception) {
        LOG.error(e)
        null
    }

    protected fun getMCMod(file: JarFile): LocalModInfo? = try {
        file.use { jar ->
            jar.getEntry("mcmod.info")?.let { jar.open(it) }
                .use { stream -> stream?.let { ModJsonParser(it) }?.getMcMod() }
        }
    } catch (_: Exception) {
        null
    }

    override fun toString(): String = displayName

    companion object {
        val valuesList = entries
        private val LOG = Logger.getInstance(ModType.Companion::class.java)

        fun File.toModType(): ModType? = try {
            this.toJarFile()?.toModType()
        } catch (e: Exception) {
            LOG.error(e)
            null
        }

        fun JarFile.toModType(): ModType? =
            valuesList.firstOrNull {
                if (it == JavaAgent && this.isJavaAgent()) return@firstOrNull true
                it.getEntry(this) != null
            }

        fun String.toModType(): ModType? = valuesList.firstOrNull { it.displayName.equals(this, ignoreCase = true) }

        fun File.toModTypes(): List<ModType> = try {
            this.toJarFile().use { jar -> valuesList.filter { it.getEntry(jar!!) != null } }
        } catch (e: Exception) {
            LOG.error(e)
            emptyList()
        }

        fun VirtualFile.toModTypes(): List<ModType> = this.toFile().toModTypes()
    }
}
