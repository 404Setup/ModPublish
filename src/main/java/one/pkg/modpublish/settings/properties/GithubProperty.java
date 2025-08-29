package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import one.pkg.modpublish.data.internel.Info;
import org.jetbrains.annotations.NotNull;

public record GithubProperty(@NotNull Info token, @NotNull String repo,
                             @NotNull String branch) implements PropertyBase {
    public static GithubProperty getInstance(PropertiesComponent properties) {
        return new GithubProperty(PID.GithubToken.getProtect(properties),
                PID.GithubRepo.get(properties),
                PID.GithubBranch.get(properties));
    }

    @Override
    public boolean isEnabled() {
        return !token.data().trim().isEmpty() && !repo.trim().isEmpty() &&
                !branch.trim().isEmpty();
    }
}