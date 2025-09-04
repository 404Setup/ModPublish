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

import java.util.List;

/**
 * Represents a version constraint that can evaluate whether specific versions satisfy
 * its defined rules and conditions.
 * <p>
 * This interface is designed to allow diverse implementations for different types
 * of version constraints, such as ranges, exact matches, or semantic versioning rules.
 * <p>
 * The constraints may define minimum and/or maximum boundary conditions, equality rules,
 * or complex logical combinations of multiple constraints.
 */
@SuppressWarnings("unused")
public interface VersionConstraint {
    /**
     * Determines if the given version satisfies the constraints defined by this implementation.
     *
     * @param version the version to be checked against the constraints
     * @return true if the version satisfies the constraints, false otherwise
     */
    boolean satisfies(@NotNull Version version);

    /**
     * Retrieves the original constraint string as it was input or defined.
     *
     * @return the original version constraint string that was used to create this constraint.
     */
    @NotNull
    String getOriginalConstraint();

    /**
     * Retrieves a list of version constraints represented as strings.
     * <p>
     * This method aggregates all conditions that define valid versions and returns them
     * in a standardized string format for further interpretation or display.
     *
     * @return a list of strings where each string specifies a version constraint.
     */
    @NotNull
    List<String> getVersions();

    /**
     * Retrieves the lowest version that satisfies the version constraint.
     * <p>
     * The result depends on how the specific implementation computes the lowest version.
     * It may return null if the constraint defines no minimum version.
     *
     * @return the lowest version as a string
     */
    @NotNull
    String getLowVersion();

    /**
     * Retrieves the highest version defined by the version constraint.
     * <p>
     * Depending on the implementing class, this may involve calculating the
     * maximum version based on one or more underlying version constraints.
     *
     * @return the maximum version as a String
     */
    @NotNull
    String getMaxVersion();

}