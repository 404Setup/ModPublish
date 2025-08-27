package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import one.pkg.modpublish.protect.HardwareFingerprint;
import one.pkg.modpublish.protect.Protect;
import one.pkg.modpublish.settings.ModPublishSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public class Properties {
    public static PropertiesComponent getPropertiesComponent(Project project) {
        return PropertiesComponent.getInstance(project);
    }

    public static @NotNull Info getProtectValue(@NotNull Project project, @NotNull String dataKey) {
        return getProtectValue(getPropertiesComponent(project), dataKey);
    }

    public static @NotNull Info getProtectValue(@NotNull PropertiesComponent properties, @NotNull String dataKey) {
        String v = properties.getValue(dataKey);
        if (v == null || v.isBlank()) {
            ModPublishSettings.State state =
                    Objects.requireNonNull(ModPublishSettings.getInstance().getState());
            return switch (dataKey) {
                case "modpublish.modrinth.token" -> Info.of(state.getModrinthToken(), true);
                case "modpublish.modrinth.testToken" -> Info.of(state.getModrinthTestToken(), true);
                case "modpublish.curseforge.token" -> Info.of(state.getCurseforgeToken(), true);
                case "modpublish.curseforge.studioToken" -> Info.of(state.getCurseforgeStudioToken(), true);
                case "modpublish.github.token" -> Info.of(state.getGithubToken(), true);
                case "modpublish.gitlab.token" -> Info.of(state.getGitlabToken(), true);
                default -> Info.INSTANCE;
            };
        }
        String r = Protect.decryptString(properties.getValue(dataKey), HardwareFingerprint.generateSecureProjectKey());
        return r == null || r.isBlank() ? Info.INSTANCE : Info.of(r);
    }

    public static void setProtectValue(@NotNull Project project, @NotNull String dataKey, @NotNull String data) {
        setProtectValue(getPropertiesComponent(project), dataKey, data);
    }

    public static void setProtectValue(@NotNull PropertiesComponent properties, @NotNull String dataKey, @NotNull String data) {
        if (dataKey.isBlank()) return;
        properties.setValue(dataKey, data.isBlank() ? null : Protect.encryptString(data, HardwareFingerprint.generateSecureProjectKey()));
    }

    public static Property getProperties(@NotNull PropertiesComponent properties) {
        return Property.getInstance(properties);
    }
}
