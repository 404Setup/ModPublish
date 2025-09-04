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

package one.pkg.modpublish.util.resources;

import com.google.gson.reflect.TypeToken;
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.local.SupportedInfo;
import one.pkg.modpublish.util.io.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LocalResources {
    public static final TypeToken<List<DependencyInfo>> dpType = new TypeToken<>() {
    };
    private static final TypeToken<List<MinecraftVersion>> mvType = new TypeToken<>() {
    };
    private static final TypeToken<List<LauncherInfo>> liType = new TypeToken<>() {
    };

    private LocalResources() {
    }

    public static SupportedInfo getSupportedInfo() {
        try (InputStream supportedStream = LocalResources.class.getResourceAsStream("/META-INF/supported.info.json")) {
            if (supportedStream == null) {
                throw new Exception("supported.info.json not found");
            }
            try (InputStreamReader reader = new InputStreamReader(supportedStream)) {
                return JsonParser.fromJson(reader, SupportedInfo.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<LauncherInfo> getLauncherInfo() {
        try (InputStream launcherStream = LocalResources.class.getResourceAsStream("/META-INF/launcher.info.json")) {
            if (launcherStream == null) {
                throw new Exception("launcher.info.json not found");
            }
            try (InputStreamReader reader = new InputStreamReader(launcherStream)) {
                return JsonParser.fromJson(reader, liType);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<MinecraftVersion> getMinecraftVersions() {
        try (InputStream stream = LocalResources.class.getResourceAsStream("/META-INF/minecraft.version.json")) {
            if (stream == null) {
                throw new Exception("minecraft.version.json not found");
            }
            try (InputStreamReader reader = new InputStreamReader(stream)) {
                return JsonParser.fromJson(reader, mvType);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
