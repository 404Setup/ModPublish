package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import one.pkg.modpublish.version.constraint.VersionConstraint;

import java.util.Collections;
import java.util.List;

public class TildeConstraint implements VersionConstraint {
    private final Version baseVersion;
    private final String original;

    public TildeConstraint(String versionStr) {
        this.baseVersion = new Version(versionStr.substring(1));
        this.original = versionStr;
    }

    @Override
    public boolean satisfies(Version version) {
        if (version.compareTo(baseVersion) < 0) return false;
        Version upperBound = new Version(baseVersion.getMajor(), baseVersion.getMinor() + 1, 0);
        return version.compareTo(upperBound) < 0;
    }

    @Override
    public List<String> getVersions() {
        return Collections.singletonList(">=" + baseVersion + " <" + baseVersion.getMajor() + "." + (baseVersion.getMinor() + 1) + ".0");
    }

    @Override
    public String getLowVersion() {
        return baseVersion.toString();
    }

    @Override
    public String getMaxVersion() {
        return baseVersion.getMajor() + "." + (baseVersion.getMinor() + 1) + ".0";
    }

    @Override
    public String original() {
        return original;
    }
}
