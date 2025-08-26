package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;

public interface PropertyBase {
    static PropertyBase getInstance(Class<?> clazz, PropertiesComponent properties) {
        if (clazz.isAssignableFrom(ModrinthProperty.class)) {
            return ModrinthProperty.getInstance(properties);
        } else if (clazz.isAssignableFrom(CurseforgeProperty.class)) {
            return CurseforgeProperty.getInstance(properties);
        } else if (clazz.isAssignableFrom(GithubProperty.class)) {
            return GithubProperty.getInstance(properties);
        } else if (clazz.isAssignableFrom(GitlabProperty.class)) {
            return GitlabProperty.getInstance(properties);
        } else {
            throw new IllegalArgumentException("Unknown property class: " + clazz.getName());
        }
    }

    boolean isEnabled();
}