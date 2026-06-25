package one.pkg.modpublish.version.constraint;


import one.pkg.modpublish.version.Version;
import one.pkg.modpublish.version.constraint.VersionConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record CompositeConstraint(List<one.pkg.modpublish.version.constraint.VersionConstraint> constraints, String original) implements one.pkg.modpublish.version.constraint.VersionConstraint {
    public CompositeConstraint(List<one.pkg.modpublish.version.constraint.VersionConstraint> constraints, String original) {
        this.constraints = new ArrayList<>(constraints);
        this.original = original;
    }

    @Override
    public boolean satisfies(Version version) {
        return constraints.stream().allMatch(constraint -> constraint.satisfies(version));
    }

    @Override
    public List<String> getVersions() {
        return constraints.stream()
                .flatMap(constraint -> constraint.getVersions().stream())
                .collect(Collectors.toList());
    }

    @Override
    public String getLowVersion() {
        return constraints.stream()
                .map(one.pkg.modpublish.version.constraint.VersionConstraint::getLowVersion)
                .filter(s -> !s.isEmpty())
                .map(Version::new)
                .max(Version::compareTo)
                .map(Version::toString)
                .orElse("");
    }

    @Override
    public String getMaxVersion() {
        return constraints.stream()
                .map(VersionConstraint::getMaxVersion)
                .filter(s -> !s.isEmpty())
                .map(Version::new)
                .min(Version::compareTo)
                .map(Version::toString)
                .orElse("");
    }
}
