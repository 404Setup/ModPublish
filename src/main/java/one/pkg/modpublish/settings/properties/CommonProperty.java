package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

public record CommonProperty(@NotNull String versionFormat) implements PropertyBase {
    public static CommonProperty getInstance(PropertiesComponent properties) {
        return new CommonProperty(PID.CommonVersionFormat.get(properties));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
