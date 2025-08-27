package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

public record ModrinthProperty(@NotNull Info token, @NotNull Info testToken, @NotNull String modid,
                               @NotNull String testModId) implements PropertyBase {
    public static ModrinthProperty getInstance(PropertiesComponent properties) {
        Info i = Properties.getProtectValue(properties, "modpublish.modrinth.token");
        Info t = Properties.getProtectValue(properties, "modpublish.modrinth.testToken");
        return new ModrinthProperty(i, t,
                properties.getValue("modpublish.modrinth.modid", ""),
                properties.getValue("modpublish.modrinth.testModId", ""));
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