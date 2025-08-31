package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Experimental
@SuppressWarnings("unused")
public class TildeConstraint implements VersionConstraint {
    private final Version baseVersion;
    private final String original;

    public TildeConstraint(String versionStr) {
        this.original = versionStr;
        this.baseVersion = new Version(versionStr.substring(1));
    }

    @Override
    public boolean satisfies(Version version) {
        if (version.compareTo(baseVersion) < 0) return false;

        Version upperBound = new Version(baseVersion.getMajor(),
                baseVersion.getMinor() + 1, 0);
        return version.compareTo(upperBound) < 0;
    }

    @Override
    public String getOriginalConstraint() {
        return original;
    }

    @Override
    public List<String> getVersions() {
        List<String> versions = new ArrayList<>();
        versions.add(">=" + baseVersion + " <" + baseVersion.getMajor() + "." + (baseVersion.getMinor() + 1) + ".0");
        return versions;
    }

    @Override
    public String getLowVersion() {
        return baseVersion.toString();
    }

    @Override
    public String getMaxVersion() {
        return baseVersion.getMajor() + "." + (baseVersion.getMinor() + 1) + ".0";
    }

}
