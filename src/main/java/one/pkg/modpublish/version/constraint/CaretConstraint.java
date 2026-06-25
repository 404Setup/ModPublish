package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import one.pkg.modpublish.version.constraint.VersionConstraint;

import java.util.Collections;
import java.util.List;

public class CaretConstraint implements VersionConstraint {
    private final Version baseVersion;
    private final String original;

    public CaretConstraint(String versionStr) {
        this.baseVersion = new Version(versionStr.substring(1));
        this.original = versionStr;
    }

    @Override
    public boolean satisfies(Version version) {
        return version.compareTo(baseVersion) >= 0 &&
                version.compareTo(new Version(baseVersion.getMajor() + 1, 0, 0)) < 0;
    }

    @Override
    public String original() {
        return original;
    }

    @Override
    public List<String> getVersions() {
        return Collections.singletonList(">=" + baseVersion + " <" + (baseVersion.getMajor() + 1) + ".0.0");
    }

    @Override
    public String getLowVersion() {
        return baseVersion.toString();
    }

    @Override
    public String getMaxVersion() {
        return (baseVersion.getMajor() + 1) + ".0.0";
    }
}
