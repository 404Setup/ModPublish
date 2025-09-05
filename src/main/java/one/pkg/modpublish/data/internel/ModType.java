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

package one.pkg.modpublish.data.internel;

import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import one.pkg.modpublish.util.io.FileAPI;
import one.pkg.modpublish.util.metadata.ModJsonParser;
import one.pkg.modpublish.util.metadata.ModTomlParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Getter
public enum ModType {
    Fabric("fabric.mod.json", "Fabric") {
        @Override
        @Nullable
        public LocalModInfo getMod(@NotNull File file) {
            return getFabricMod(file);
        }
    }, Quilt("quilt.mod.json", "Quilt") {
        @Override
        @Nullable
        public LocalModInfo getMod(@NotNull File file) {
            return getFabricMod(file);
        }
    },
    Forge("META-INF/mods.toml", "Forge") {
        @Override
        @Nullable
        public LocalModInfo getMod(@NotNull File file) {
            return getForgeMod(file);
        }
    }, NeoForge("META-INF/neoforge.mods.toml", "NeoForge") {
        @Override
        @Nullable
        public LocalModInfo getMod(@NotNull File file) {
            return getForgeMod(file);
        }
    }, Rift("riftmod.json", "Rift") {
        @Override
        @Nullable
        public LocalModInfo getMod(@NotNull File file) {
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
        return of(FileAPI.toFile(file));
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
        return getAll(FileAPI.toFile(file));
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
            if (type.name.toLowerCase().equalsIgnoreCase(name)) return type;
        }
        return null;
    }

    @Nullable
    LocalModInfo getFabricMod(@NotNull File file) {
        try (JarFile j = FileAPI.toJarFile(file);
             InputStream s = getStream(j)) {
            ModJsonParser parser = new ModJsonParser(s);
            return parser.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    LocalModInfo getForgeMod(@NotNull File file) {
        try (JarFile j = FileAPI.toJarFile(file);
             InputStream s = getStream(j);
             ModTomlParser parser = ModTomlParser.of(s)) {
            return parser.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public LocalModInfo getMod(@NotNull VirtualFile file) {
        return getMod(FileAPI.toFile(file));
    }

    @Nullable
    public abstract LocalModInfo getMod(@NotNull File file);

    @Nullable
    public ZipEntry getEntry(JarFile jar) {
        return jar.getEntry(fileName);
    }

    @Nullable
    public InputStream getStream(JarFile jar) throws IOException {
        return FileAPI.open(jar, getEntry(jar));
    }

    @Override
    public String toString() {
        return name;
    }
}
