package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
@SuppressWarnings("unused")
public class CompositeConstraint implements VersionConstraint {
    private final List<VersionConstraint> constraints;
    private final String original;

    public CompositeConstraint(List<VersionConstraint> constraints, String original) {
        this.constraints = new ArrayList<>(constraints);
        this.original = original;
    }

    @Override
    public boolean satisfies(Version version) {
        return constraints.stream().allMatch(constraint -> constraint.satisfies(version));
    }

    @Override
    public String getOriginalConstraint() {
        return original;
    }

    @Override
    public List<String> getVersions() {
        List<String> allVersions = new ArrayList<>();
        for (VersionConstraint constraint : constraints) {
            allVersions.addAll(constraint.getVersions());
        }
        return allVersions;
    }

    @Override
    public String getLowVersion() {
        String lowest = null;
        for (VersionConstraint constraint : constraints) {
            String low = constraint.getLowVersion();
            if (low != null) {
                if (lowest == null) {
                    lowest = low;
                } else {
                    Version lowVersion = new Version(low);
                    Version currentLowest = new Version(lowest);
                    if (lowVersion.compareTo(currentLowest) > 0) {
                        lowest = low;
                    }
                }
            }
        }
        return lowest;
    }

    @Override
    public String getMaxVersion() {
        String highest = null;
        for (VersionConstraint constraint : constraints) {
            String max = constraint.getMaxVersion();
            if (max != null) {
                if (highest == null) {
                    highest = max;
                } else {
                    Version maxVersion = new Version(max);
                    Version currentHighest = new Version(highest);
                    if (maxVersion.compareTo(currentHighest) < 0) {
                        highest = max;
                    }
                }
            }
        }
        return highest;
    }

}
