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
