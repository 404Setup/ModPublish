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

package one.pkg.modpublish.util.version.constraint;

import one.pkg.modpublish.util.version.Version;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class ExactVersionConstraint implements VersionConstraint {
    private final Version targetVersion;
    private final String original;

    public ExactVersionConstraint(String versionStr) {
        this.original = versionStr;
        this.targetVersion = new Version(versionStr);
    }

    @Override
    public boolean satisfies(@NotNull Version version) {
        return targetVersion.equals(version);
    }

    @Override
    public @NotNull String getOriginalConstraint() {
        return original;
    }

    @Override
    public @NotNull List<String> getVersions() {
        return Collections.singletonList(targetVersion.toString());
    }

    @Override
    public @NotNull String getLowVersion() {
        return targetVersion.toString();
    }

    @Override
    public @NotNull String getMaxVersion() {
        return targetVersion.toString();
    }

}
