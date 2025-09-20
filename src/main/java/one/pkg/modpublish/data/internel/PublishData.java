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

import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.local.SupportedInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public record PublishData(@NotNull String versionName, @NotNull String versionNumber, @NotNull Selector enabled,
                          @NotNull ReleaseChannel releaseChannel, @NotNull List<LauncherInfo> loaders,
                          @NotNull SupportedInfo supportedInfo, @NotNull List<MinecraftVersion> minecraftVersions,
                          @NotNull String changelog, @NotNull List<DependencyInfo> dependencies,
                          @NotNull File[] files) {
}