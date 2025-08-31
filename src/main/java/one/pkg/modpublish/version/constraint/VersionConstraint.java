package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import org.jetbrains.annotations.ApiStatus;

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
@ApiStatus.Experimental
@SuppressWarnings("unused")
public interface VersionConstraint {
    /**
     * Determines if the given version satisfies the constraints defined by this implementation.
     *
     * @param version the version to be checked against the constraints
     * @return true if the version satisfies the constraints, false otherwise
     */
    boolean satisfies(Version version);

    /**
     * Retrieves the original constraint string as it was input or defined.
     *
     * @return the original version constraint string that was used to create this constraint.
     */
    String getOriginalConstraint();

    /**
     * Retrieves a list of version constraints represented as strings.
     * <p>
     * This method aggregates all conditions that define valid versions and returns them
     * in a standardized string format for further interpretation or display.
     *
     * @return a list of strings where each string specifies a version constraint.
     */
    List<String> getVersions();

    /**
     * Retrieves the lowest version that satisfies the version constraint.
     * <p>
     * The result depends on how the specific implementation computes the lowest version.
     * It may return null if the constraint defines no minimum version.
     *
     * @return the lowest version as a string, or null if not defined
     */
    String getLowVersion();

    /**
     * Retrieves the highest version defined by the version constraint.
     * <p>
     * Depending on the implementing class, this may involve calculating the
     * maximum version based on one or more underlying version constraints.
     *
     * @return the maximum version as a String, or null if no maximum version is defined.
     */
    String getMaxVersion();

}