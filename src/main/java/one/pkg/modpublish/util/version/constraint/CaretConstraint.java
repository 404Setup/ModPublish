package one.pkg.modpublish.util.version.constraint;

import one.pkg.modpublish.util.version.Version;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class CaretConstraint implements VersionConstraint {
    private final Version baseVersion;
    private final String original;

    public CaretConstraint(String versionStr) {
        this.original = versionStr;
        this.baseVersion = new Version(versionStr.substring(1));
    }

    @Override
    public boolean satisfies(@NotNull Version version) {
        if (version.compareTo(baseVersion) < 0) return false;

        Version upperBound = new Version(baseVersion.getMajor() + 1, 0, 0);
        return version.compareTo(upperBound) < 0;
    }

    @Override
    public @NotNull String getOriginalConstraint() {
        return original;
    }

    @Override
    public @NotNull List<String> getVersions() {
        List<String> versions = new ArrayList<>();
        versions.add(">=" + baseVersion + " <" + (baseVersion.getMajor() + 1) + ".0.0");
        return versions;
    }

    @Override
    public @NotNull String getLowVersion() {
        return baseVersion.toString();
    }

    @Override
    public @NotNull String getMaxVersion() {
        return (baseVersion.getMajor() + 1) + ".0.0";
    }

}
