package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
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
}