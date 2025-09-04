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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class RangeConstraint implements VersionConstraint {
    private final Version minVersion;
    private final Version maxVersion;
    private final boolean includeMin;
    private final boolean includeMax;
    private final String original;

    public RangeConstraint(Version minVersion, Version maxVersion,
                           boolean includeMin, boolean includeMax, String original) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.includeMin = includeMin;
        this.includeMax = includeMax;
        this.original = original;
    }

    @Override
    public boolean satisfies(@NotNull Version version) {
        if (minVersion != null) {
            int minCompare = version.compareTo(minVersion);
            if (includeMin && minCompare < 0) return false;
            if (!includeMin && minCompare <= 0) return false;
        }

        if (maxVersion != null) {
            int maxCompare = version.compareTo(maxVersion);
            if (includeMax && maxCompare > 0) return false;
            return includeMax || maxCompare < 0;
        }

        return true;
    }

    @Override
    public @NotNull String getOriginalConstraint() {
        return original;
    }

    @Override
    public @NotNull List<String> getVersions() {
        List<String> versions = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        if (minVersion != null)
            sb.append(includeMin ? ">=" : ">").append(minVersion);


        if (maxVersion != null) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(includeMax ? "<=" : "<").append(maxVersion);
        }

        versions.add(sb.toString());
        return versions;
    }

    @Override
    public @NotNull String getLowVersion() {
        return minVersion != null ? minVersion.toString() : "";
    }

    @Override
    public @NotNull String getMaxVersion() {
        return maxVersion != null ? maxVersion.toString() : "";
    }

}
