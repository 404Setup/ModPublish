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

package one.pkg.modpublish.util.io;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileAPI {
    static {
        var p = Paths.get(System.getProperty("user.home"), ".modpublish").toFile();
        if (!p.exists()) {
            p.mkdirs();
        } else if (p.isFile()) {
            p.delete();
            p.mkdirs();
        }
    }

    public static @NotNull File toFile(@NotNull VirtualFile file) {
        return new File(file.getPath());
    }

    public static @Nullable JarFile toJarFile(@NotNull VirtualFile file) {
        return toJarFile(file.getPath());
    }

    public static @Nullable JarFile toJarFile(@NotNull File file) {
        return toJarFile(file.getPath());
    }

    public static @Nullable JarFile toJarFile(@NotNull String path) {
        try {
            return new JarFile(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static @NotNull Path getUserData(@NotNull String name) {
        return Paths.get(System.getProperty("user.home"), ".modpublish", name);
    }

    public static @NotNull File getUserDataFile(@NotNull String name) {
        return getUserData(name).toFile();
    }

    public static InputStream open(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        return zipFile.getInputStream(zipEntry);
    }
}
