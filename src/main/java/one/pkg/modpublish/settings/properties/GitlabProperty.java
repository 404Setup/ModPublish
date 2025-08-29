package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import one.pkg.modpublish.data.internel.Info;
import org.jetbrains.annotations.NotNull;

public record GitlabProperty(@NotNull Info token, @NotNull String repo, @NotNull String branch) implements PropertyBase {
    public static GitlabProperty getInstance(PropertiesComponent properties) {
        return new GitlabProperty(PID.GitlabToken.getProtect(properties),
                PID.GitlabRepo.get(properties),
                PID.GitlabBranch.get(properties));
    }

    @Override
    public boolean isEnabled() {
        return !token.data().trim().isEmpty() && !repo.trim().isEmpty() &&
                !branch.trim().isEmpty();
    }
}