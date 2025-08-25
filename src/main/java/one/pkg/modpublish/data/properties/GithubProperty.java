package one.pkg.modpublish.data.properties;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

public record GithubProperty(@NotNull String token, @NotNull String repo, @NotNull String branch,
                             boolean globalData) implements PropertyBase {
    public static GithubProperty getInstance(PropertiesComponent properties) {
        Info i = Properties.getProtectValue(properties, "modpublish.github.token");
        return new GithubProperty(i.data(),
                properties.getValue("modpublish.github.repo", ""),
                properties.getValue("modpublish.github.branch", ""),
                i.globalData());
    }

    @Override
    public boolean isEnabled() {
        return !token.trim().isEmpty() && !repo.trim().isEmpty() &&
                !branch.trim().isEmpty();
    }
}