package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

public record GitlabProperty(@NotNull String token, @NotNull String repo, @NotNull String branch,
                             boolean globalData) implements PropertyBase {
    public static GitlabProperty getInstance(PropertiesComponent properties) {
        Info i = Properties.getProtectValue(properties, "modpublish.gitlab.token");
        return new GitlabProperty(i.data(),
                properties.getValue("modpublish.gitlab.repo", ""),
                properties.getValue("modpublish.gitlab.branch", ""),
                i.globalData());
    }

    @Override
    public boolean isEnabled() {
        return !token.trim().isEmpty() && !repo.trim().isEmpty() &&
                !branch.trim().isEmpty();
    }
}