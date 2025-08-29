package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import one.pkg.modpublish.data.internel.Info;
import org.jetbrains.annotations.NotNull;

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