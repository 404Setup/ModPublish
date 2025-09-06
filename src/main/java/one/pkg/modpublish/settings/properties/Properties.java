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

package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import one.pkg.modpublish.data.internel.Info;
import one.pkg.modpublish.settings.ModPublishSettings;
import one.pkg.modpublish.util.protect.HardwareFingerprint;
import one.pkg.modpublish.util.protect.Protect;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public class Properties {
    public static PropertiesComponent getPropertiesComponent(Project project) {
        return PropertiesComponent.getInstance(project);
    }

    public static @NotNull Info getProtectValue(@NotNull Project project, @NotNull PID dataKey) {
        return getProtectValue(getPropertiesComponent(project), dataKey);
    }

    public static @NotNull Info getProtectValue(@NotNull PropertiesComponent properties, @NotNull PID dataKey) {
        String v = dataKey.get(properties);
        if (v.isBlank()) {
            ModPublishSettings.State state =
                    Objects.requireNonNull(ModPublishSettings.getInstance().getState());
            return switch (dataKey) {
                case ModrinthToken -> state.getModrinthToken();
                case ModrinthTestToken -> state.getModrinthTestToken();
                case CurseForgeToken -> state.getCurseforgeToken();
                case CurseForgeStudioToken -> state.getCurseforgeStudioToken();
                case GithubToken -> state.getGithubToken();
                default -> Info.INSTANCE;
            };
        }
        String r = Protect.decryptString(v, HardwareFingerprint.generateSecureProjectKey());
        if (r.isBlank() || r.equals(v)) return Info.of(r, true, false);
        return Info.of(r);
    }

    public static void setProtectValue(@NotNull Project project, @NotNull String dataKey, @NotNull String data) {
        setProtectValue(getPropertiesComponent(project), dataKey, data);
    }

    public static void setProtectValue(@NotNull PropertiesComponent properties, @NotNull String dataKey, @NotNull String data) {
        if (dataKey.isBlank()) return;
        properties.setValue(dataKey, data.isBlank() ? null : Protect.encryptString(data, HardwareFingerprint.generateSecureProjectKey()));
    }

    public static Property getProperties(@NotNull PropertiesComponent properties) {
        return Property.getInstance(properties);
    }

    public static Property getProperties(@NotNull Project project) {
        return Property.getInstance(getPropertiesComponent(project));
    }
}
