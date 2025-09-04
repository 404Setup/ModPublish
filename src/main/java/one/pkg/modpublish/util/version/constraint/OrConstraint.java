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
public class OrConstraint implements VersionConstraint {
    private final List<VersionConstraint> constraints;
    private final String original;

    public OrConstraint(List<VersionConstraint> constraints, String original) {
        this.constraints = new ArrayList<>(constraints);
        this.original = original;
    }

    @Override
    public boolean satisfies(@NotNull Version version) {
        return constraints.stream().anyMatch(constraint -> constraint.satisfies(version));
    }

    @Override
    public @NotNull String getOriginalConstraint() {
        return original;
    }

    @Override
    public @NotNull List<String> getVersions() {
        List<String> allVersions = new ArrayList<>();
        for (VersionConstraint constraint : constraints) {
            allVersions.addAll(constraint.getVersions());
        }
        return allVersions;
    }

    @Override
    public @NotNull String getLowVersion() {
        String lowest = null;
        for (VersionConstraint constraint : constraints) {
            String low = constraint.getLowVersion();
            if (!low.isEmpty()) {
                if (lowest == null) {
                    lowest = low;
                } else {
                    Version lowVersion = new Version(low);
                    Version currentLowest = new Version(lowest);
                    if (lowVersion.compareTo(currentLowest) < 0) {
                        lowest = low;
                    }
                }
            }
        }
        return lowest == null ? "" : lowest;
    }

    @Override
    public @NotNull String getMaxVersion() {
        String highest = null;
        for (VersionConstraint constraint : constraints) {
            String max = constraint.getMaxVersion();
            if (!max.isEmpty()) {
                if (highest == null) {
                    highest = max;
                } else {
                    Version maxVersion = new Version(max);
                    Version currentHighest = new Version(highest);
                    if (maxVersion.compareTo(currentHighest) > 0) {
                        highest = max;
                    }
                }
            }
        }
        return highest == null ? "" : highest;
    }
}