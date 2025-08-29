package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import one.pkg.modpublish.data.internel.Info;
import org.jetbrains.annotations.NotNull;

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