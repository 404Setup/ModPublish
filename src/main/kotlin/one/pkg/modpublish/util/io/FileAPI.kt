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

import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object FileAPI {
    init {
        val p = Paths.get(System.getProperty("user.home"), ".modpublish").toFile()
        if (!p.exists()) {
            p.mkdirs()
        } else if (p.isFile()) {
            p.delete()
            p.mkdirs()
        }
    }

    @JvmStatic
    fun VirtualFile.toFile(): File {
        return File(this.path)
    }

    fun VirtualFile.toJarFile(): JarFile? {
        return toJarFile(this.path)
    }

    @JvmStatic
    fun File.toJarFile(): JarFile? {
        return toJarFile(this.path)
    }

    fun toJarFile(path: String): JarFile? {
        try {
            return JarFile(path)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getUserData(name: String): Path {
        return Paths.get(System.getProperty("user.home"), ".modpublish", name)
    }

    @JvmStatic
    fun getUserDataFile(name: String): File {
        return getUserData(name).toFile()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun open(zipFile: ZipFile, zipEntry: ZipEntry): InputStream? {
        return zipFile.getInputStream(zipEntry)
    }
}
