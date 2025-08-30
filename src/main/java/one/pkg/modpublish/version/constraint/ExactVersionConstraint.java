package one.pkg.modpublish.version.constraint;

import one.pkg.modpublish.version.Version;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@ApiStatus.Experimental
public class ExactVersionConstraint implements VersionConstraint {
    private final Version targetVersion;
    private final String original;

    public ExactVersionConstraint(String versionStr) {
        this.original = versionStr;
        this.targetVersion = new Version(versionStr);
    }

    @Override
    public boolean satisfies(Version version) {
        return targetVersion.equals(version);
    }

    @Override
    public String getOriginalConstraint() {
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
