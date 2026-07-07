package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import one.pkg.modpublish.version.constraint.VersionConstraint;

import java.util.Collections;
import java.util.List;

public record RangeConstraint(Version minVer, Version maxVer, boolean includeMin,
                              boolean includeMax, String original) implements VersionConstraint {

    @Override
    public boolean satisfies(Version version) {
        if (minVer != null) {
            int cmp = version.compareTo(minVer);
            if ((includeMin && cmp < 0) || (!includeMin && cmp <= 0)) return false;
        }

        if (maxVer != null) {
            int cmp = version.compareTo(maxVer);
            return (!includeMax || cmp <= 0) && (includeMax || cmp < 0);
        }

        return true;
    }

    @Override
    public List<String> getVersions() {
        StringBuilder builder = new StringBuilder();
        if (minVer != null) {
            builder.append(includeMin ? ">=" : ">").append(minVer);
        }
        if (maxVer != null) {
            if (!builder.isEmpty()) builder.append(" ");
            builder.append(includeMax ? "<=" : "<").append(maxVer);
        }
        return Collections.singletonList(builder.toString());
    }

    @Override
    public String getLowVersion() {
        return minVer != null ? minVer.toString() : "";
    }

    @Override
    public String getMaxVersion() {
        return maxVer != null ? maxVer.toString() : "";
    }
}
