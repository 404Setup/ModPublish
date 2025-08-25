package one.pkg.modpublish.data.properties;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

public record Property(@NotNull ModrinthProperty modrinth, @NotNull CurseforgeProperty curseforge,
                       @NotNull GithubProperty github, @NotNull GitlabProperty gitlab) {
    public Property(@NotNull PropertyBase m, @NotNull PropertyBase c, @NotNull PropertyBase g, @NotNull PropertyBase gla) {
        this((ModrinthProperty) m, (CurseforgeProperty) c, (GithubProperty) g, (GitlabProperty) gla);
    }

    public static Property getInstance(PropertiesComponent properties) {
        return new Property(PropertyBase.getInstance(ModrinthProperty.class, properties),
                PropertyBase.getInstance(CurseforgeProperty.class, properties),
                PropertyBase.getInstance(GithubProperty.class, properties),
                PropertyBase.getInstance(GitlabProperty.class, properties)
        );
    }
}