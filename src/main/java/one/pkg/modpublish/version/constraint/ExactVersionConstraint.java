package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import one.pkg.modpublish.version.constraint.VersionConstraint;

import java.util.Collections;
import java.util.List;

public class ExactVersionConstraint implements VersionConstraint {
    private final Version targetVersion;
    private final String original;

    public ExactVersionConstraint(String versionStr) {
        this.targetVersion = new Version(versionStr);
        this.original = versionStr;
    }

    @Override
    public boolean satisfies(Version version) {
        return targetVersion.equals(version);
    }

    @Override
    public String original() {
        return original;
    }

    @Override
    public List<String> getVersions() {
        return Collections.singletonList(targetVersion.toString());
    }

    @Override
    public String getLowVersion() {
        return targetVersion.toString();
    }

    @Override
    public String getMaxVersion() {
        return targetVersion.toString();
    }
}
