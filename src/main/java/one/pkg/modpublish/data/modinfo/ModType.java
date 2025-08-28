package one.pkg.modpublish.data.modinfo;

import com.intellij.openapi.vfs.VirtualFile;
import one.pkg.modpublish.util.VirtualFileAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public enum ModType {
    Fabric("fabric.mod.json", "fabric") {
        @Override
        @Nullable
        public String getModVersion(@NotNull File file) {
            return getFabricModVersion(file);
        }
    }, Quilt("quilt.mod.json", "quilt") {
        @Override
        @Nullable
        public String getModVersion(@NotNull File file) {
            return getFabricModVersion(file);
        }
    },
    Forge("META-INF/mods.toml", "forge") {
        @Override
        @Nullable
        public String getModVersion(@NotNull File file) {
            return getForgeModVersion(file);
        }
    }, NeoForge("META-INF/neoforge.mods.toml", "neoforge") {
        @Override
        @Nullable
        public String getModVersion(@NotNull File file) {
            return getForgeModVersion(file);
        }
    }, Rift("riftmod.json", "rift") {
        @Override
        @Nullable
        public String getModVersion(@NotNull File file) {
            // RiftMod version reading not supported
            return null;
        }
    };

    private static final ModType[] VALUES = values();
    private final String fileName;
    private final String name;

    ModType(String fileName, String name) {
        this.fileName = fileName;
        this.name = name;
    }

    @Nullable
    public static ModType of(VirtualFile file) {
        return of(VirtualFileAPI.toFile(file));
    }

    @Nullable
    public static ModType of(File file) {
        try (JarFile jar = new JarFile(file)) {
            return of(jar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static ModType of(JarFile jar) {
        for (ModType type : VALUES)
            if (type.getEntry(jar) != null) return type;
        return null;
    }

    public static List<ModType> getAll(VirtualFile file) {
        return getAll(VirtualFileAPI.toFile(file));
    }

    public static List<ModType> getAll(File file) {
        List<ModType> types = new ArrayList<>();
        try (JarFile jar = new JarFile(file)) {
            for (ModType type : VALUES)
                if (type.getEntry(jar) != null) types.add(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return types;
    }

    @Nullable
    public static ModType of(String name) {
        for (ModType type : VALUES) {
            if (type.name.equalsIgnoreCase(name)) return type;
        }
        return null;
    }

    @Nullable
    String getFabricModVersion(@NotNull File file) {
        try (JarFile j = VirtualFileAPI.toJarFile(file);
             InputStream s = getStream(j)) {
            ModJsonParser parser = new ModJsonParser(s);
            return parser.getVersion();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    String getForgeModVersion(@NotNull File file) {
        try (JarFile j = VirtualFileAPI.toJarFile(file);
             InputStream s = getStream(j);
             ModTomlParser parser = ModTomlParser.of(s)) {
            return parser.getVersion();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public String getModVersion(@NotNull VirtualFile file) {
        return getModVersion(VirtualFileAPI.toFile(file));
    }

    @Nullable
    public abstract String getModVersion(@NotNull File file);

    @Nullable
    public ZipEntry getEntry(JarFile jar) {
        return jar.getEntry(fileName);
    }

    @Nullable
    public InputStream getStream(JarFile jar) throws IOException {
        return VirtualFileAPI.open(jar, getEntry(jar));
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
