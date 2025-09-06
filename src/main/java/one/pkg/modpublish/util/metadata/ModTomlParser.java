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

package one.pkg.modpublish.util.metadata;

import com.intellij.openapi.diagnostic.Logger;
import one.pkg.modpublish.data.internel.LocalModInfo;
import one.pkg.modpublish.util.io.TomlParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

@SuppressWarnings("unused")
public record ModTomlParser(@NotNull TomlParser parser) implements AutoCloseable {
    private static final Logger LOG = Logger.getInstance(ModTomlParser.class);

    public static ModTomlParser of(@NotNull InputStream inputStream) {
        return new ModTomlParser(TomlParser.fromStream(inputStream));
    }

    @SuppressWarnings("all")
    @Nullable
    public LocalModInfo get() {
        try {
            @Nullable TomlParser mods = parser.getAsTomlParser("mods");
            if (mods == null || mods.isEmpty()) {
                LOG.warn("No mods section found in TOML file");
                return null;
            }

            String displayName = mods.getAsString("displayName");
            String version = mods.getAsString("version");

            return new LocalModInfo(displayName, version, getMinecraftVersions(mods.getAsString("modId")));
        } catch (Exception e) {
            LOG.error("Failed to parse mod.toml", e);
            return null;
        }
    }

    @NotNull
    public String getMinecraftVersions(String modId) {
        TomlParser dependencies = parser.getAsTomlParser("dependencies");
        if (dependencies != null) {
            TomlParser.TomlArray arrays = dependencies.getAsTomlArray(modId);
            if (arrays.isEmpty()) {
                return "";
            }
            for (TomlParser array : arrays) {
                if (array.getAsString("modId").equals("minecraft"))
                    return array.getAsString("versionRange");
            }
        }

        return "";
    }

    @Override
    public void close() {
    }
}