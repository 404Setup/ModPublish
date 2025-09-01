package one.pkg.modpublish.util.version.constraint;

import one.pkg.modpublish.util.version.Version;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class ExactVersionConstraint implements VersionConstraint {
    private final Version targetVersion;
    private final String original;

    public ExactVersionConstraint(String versionStr) {
        this.original = versionStr;
        this.targetVersion = new Version(versionStr);
    }

    @Override
    public boolean satisfies(@NotNull Version version) {
        return targetVersion.equals(version);
    }

    @Override
    public @NotNull String getOriginalConstraint() {
        return original;
    }

    @Override
    public @NotNull List<String> getVersions() {
        return Collections.singletonList(targetVersion.toString());
    }

    @Override
    public @NotNull String getLowVersion() {
        return targetVersion.toString();
    }

    @Override
    public @NotNull String getMaxVersion() {
        return targetVersion.toString();
    }

}
