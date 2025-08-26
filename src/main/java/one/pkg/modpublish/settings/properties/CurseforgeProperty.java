package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

public record CurseforgeProperty(@NotNull Info token, @NotNull Info studioToken, @NotNull String modid) implements PropertyBase {
    public static CurseforgeProperty getInstance(PropertiesComponent properties) {
        Info i = Properties.getProtectValue(properties, "modpublish.curseforge.token");
        Info s = Properties.getProtectValue(properties, "modpublish.curseforge.studioToken");
        return new CurseforgeProperty(i,
                s,
                properties.getValue("modpublish.curseforge.modid", ""));
    }

    @Override
    public boolean isEnabled() {
        return !token.data().trim().isEmpty() && !studioToken.data().trim().isEmpty() && !modid.trim().isEmpty();
    }
}