package one.pkg.modpublish.version.constraint;


import one.pkg.modpublish.version.Version;

import java.util.ArrayList;
import java.util.List;

public record CompositeConstraint(List<VersionConstraint> constraints, String original) implements VersionConstraint {
    public CompositeConstraint(List<VersionConstraint> constraints, String original) {
        this.constraints = new ArrayList<>(constraints);
        this.original = original;
    }

    @Override
    public boolean satisfies(Version version) {
        for (VersionConstraint constraint : constraints) {
            if (!constraint.satisfies(version)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> getVersions() {
        List<String> result = new ArrayList<>();
        for (VersionConstraint constraint : constraints) {
            result.addAll(constraint.getVersions());
        }
        return result;
    }

    @Override
    public String getLowVersion() {
        Version maxVersion = null;
        for (VersionConstraint constraint : constraints) {
            String lowVersion = constraint.getLowVersion();
            if (!lowVersion.isEmpty()) {
                Version version = new Version(lowVersion);
                if (maxVersion == null || version.compareTo(maxVersion) > 0) {
                    maxVersion = version;
                }
            }
        }
        return maxVersion != null ? maxVersion.toString() : "";
    }

    @Override
    public String getMaxVersion() {
        Version minVersion = null;
        for (VersionConstraint constraint : constraints) {
            String maxVersion = constraint.getMaxVersion();
            if (!maxVersion.isEmpty()) {
                Version version = new Version(maxVersion);
                if (minVersion == null || version.compareTo(minVersion) < 0) {
                    minVersion = version;
                }
            }
        }
        return minVersion != null ? minVersion.toString() : "";
    }
}
