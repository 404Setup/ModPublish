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
import one.pkg.modpublish.data.internel.Info;
import org.jetbrains.annotations.NotNull;

public record Property(@NotNull ModrinthProperty modrinth, @NotNull CurseForgeProperty curseforge,
                       @NotNull GithubProperty github,
                       @NotNull CommonProperty common) {
    public static Property getInstance(PropertiesComponent properties) {
        return new Property(ModrinthProperty.getInstance(properties),
                CurseForgeProperty.getInstance(properties),
                GithubProperty.getInstance(properties),
                CommonProperty.getInstance(properties)
        );
    }

    public record CommonProperty(@NotNull String versionFormat) implements PropertyBase {
        public static CommonProperty getInstance(PropertiesComponent properties) {
            return new CommonProperty(PID.CommonVersionFormat.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    public record CurseForgeProperty(@NotNull Info token, @NotNull Info studioToken,
                                     @NotNull String modid) implements PropertyBase {
        public static CurseForgeProperty getInstance(PropertiesComponent properties) {
            return new CurseForgeProperty(PID.CurseForgeToken.getProtect(properties),
                    PID.CurseForgeStudioToken.getProtect(properties),
                    PID.CurseForgeModID.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return !token.data().trim().isEmpty() && !studioToken.data().trim().isEmpty() && !modid.trim().isEmpty();
        }
    }

    public record GithubProperty(@NotNull Info token, @NotNull String repo,
                                 @NotNull String branch) implements PropertyBase {
        public static GithubProperty getInstance(PropertiesComponent properties) {
            return new GithubProperty(PID.GithubToken.getProtect(properties),
                    PID.GithubRepo.get(properties),
                    PID.GithubBranch.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return !token.data().trim().isEmpty() && !repo.trim().isEmpty();
        }
    }

    public record ModrinthProperty(@NotNull Info token,@NotNull String modid) implements PropertyBase {
        public static ModrinthProperty getInstance(PropertiesComponent properties) {
            return new ModrinthProperty(PID.ModrinthToken.getProtect(properties),
                    PID.ModrinthModID.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return !token.data().trim().isEmpty() && !modid.trim().isEmpty();
        }
    }
}