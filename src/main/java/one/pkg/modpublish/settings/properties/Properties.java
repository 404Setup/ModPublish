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

    public static @NotNull Info getProtectValue(@NotNull Project project, @NotNull PID dataKey) {
        return getProtectValue(getPropertiesComponent(project), dataKey);
    }

    public static @NotNull Info getProtectValue(@NotNull PropertiesComponent properties, @NotNull PID dataKey) {
        String v = dataKey.get(properties);
        if (v == null || v.isBlank()) {
            ModPublishSettings.State state =
                    Objects.requireNonNull(ModPublishSettings.getInstance().getState());
            return switch (dataKey) {
                case ModrinthToken -> Info.of(state.getModrinthToken(), true);
                case ModrinthTestToken -> Info.of(state.getModrinthTestToken(), true);
                case CurseForgeToken -> Info.of(state.getCurseforgeToken(), true);
                case CurseForgeStudioToken -> Info.of(state.getCurseforgeStudioToken(), true);
                case GithubToken -> Info.of(state.getGithubToken(), true);
                case GitlabToken -> Info.of(state.getGitlabToken(), true);
                default -> Info.INSTANCE;
            };
        }
        String r = Protect.decryptString(v, HardwareFingerprint.generateSecureProjectKey());
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

    public static Property getProperties(@NotNull Project project) {
        return Property.getInstance(getPropertiesComponent(project));
    }
}
