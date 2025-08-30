package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
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
    public boolean satisfies(Version version) {
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
    public String getOriginalConstraint() {
        return original;
    }

    @Override
    public List<String> getVersions() {
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
    public String getLowVersion() {
        return minVersion != null ? minVersion.toString() : null;
    }

    @Override
    public String getMaxVersion() {
        return maxVersion != null ? maxVersion.toString() : null;
    }

}
