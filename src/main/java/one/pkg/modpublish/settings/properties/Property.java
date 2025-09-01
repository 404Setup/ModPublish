package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import one.pkg.modpublish.data.internel.Info;
import org.jetbrains.annotations.NotNull;

public record Property(@NotNull ModrinthProperty modrinth, @NotNull CurseForgeProperty curseforge,
                       @NotNull GithubProperty github, @NotNull GitlabProperty gitlab,
                       @NotNull CommonProperty common) {
    public static Property getInstance(PropertiesComponent properties) {
        return new Property(ModrinthProperty.getInstance(properties),
                CurseForgeProperty.getInstance(properties),
                GithubProperty.getInstance(properties),
                GitlabProperty.getInstance(properties),
                CommonProperty.getInstance(properties)
        );
    }

    public record CommonProperty(@NotNull String versionFormat) implements PropertyBase {
        public static CommonProperty getInstance(PropertiesComponent properties) {
            return new CommonProperty(PID.CommonVersionFormat.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    public record CurseForgeProperty(@NotNull Info token, @NotNull Info studioToken,
                                     @NotNull String modid) implements PropertyBase {
        public static CurseForgeProperty getInstance(PropertiesComponent properties) {
            return new CurseForgeProperty(PID.CurseForgeToken.getProtect(properties),
                    PID.CurseForgeStudioToken.getProtect(properties),
                    PID.CurseForgeModID.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return !token.data().trim().isEmpty() && !studioToken.data().trim().isEmpty() && !modid.trim().isEmpty();
        }
    }

    public record GithubProperty(@NotNull Info token, @NotNull String repo,
                                 @NotNull String branch) implements PropertyBase {
        public static GithubProperty getInstance(PropertiesComponent properties) {
            return new GithubProperty(PID.GithubToken.getProtect(properties),
                    PID.GithubRepo.get(properties),
                    PID.GithubBranch.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return !token.data().trim().isEmpty() && !repo.trim().isEmpty();
        }
    }

    public record GitlabProperty(@NotNull Info token, @NotNull String repo,
                                 @NotNull String branch) implements PropertyBase {
        public static GitlabProperty getInstance(PropertiesComponent properties) {
            return new GitlabProperty(PID.GitlabToken.getProtect(properties),
                    PID.GitlabRepo.get(properties),
                    PID.GitlabBranch.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return !token.data().trim().isEmpty() && !repo.trim().isEmpty();
        }
    }

    public record ModrinthProperty(@NotNull Info token, @NotNull Info testToken, @NotNull String modid,
                                   @NotNull String testModId) implements PropertyBase {
        public static ModrinthProperty getInstance(PropertiesComponent properties) {
            return new ModrinthProperty(PID.ModrinthToken.getProtect(properties),
                    PID.ModrinthTestToken.getProtect(properties),
                    PID.ModrinthModID.get(properties),
                    PID.ModrinthTestModID.get(properties));
        }

        @Override
        public boolean isEnabled() {
            return isModEnabled() || isTestEnabled();
        }

        public boolean isTestEnabled() {
            return !testToken.data().trim().isEmpty() && !testModId.trim().isEmpty();
        }

        public boolean isModEnabled() {
            return !token.data().trim().isEmpty() && !modid.trim().isEmpty();
        }
    }
}