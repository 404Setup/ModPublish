package one.pkg.modpublish.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@State(
        name = "org.intellij.sdk.settings.AppSettings",
        storages = @Storage("ModPublish.xml")
)
public final class ModPublishSettings
        implements PersistentStateComponent<ModPublishSettings.State> {

    private State myState = new State();

    public static ModPublishSettings getInstance() {
        return ApplicationManager.getApplication()
                .getService(ModPublishSettings.class);
    }

    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public static class State extends StateBase {
        @NonNls
        public String modrinthToken = "";
        @NonNls
        public String modrinthTestToken = "";
        @NonNls
        public String curseforgeToken = "";
        @NonNls
        public String curseforgeStudioToken = "";
        @NonNls
        public String githubToken = "";
        @NonNls
        public String gitlabToken = "";

        public String getModrinthToken() {
            return getDecryptedToken(modrinthToken);
        }

        public void updateModrinthToken(String token) {
            this.modrinthToken = encryptToken(token);
        }

        public String getModrinthTestToken() {
            return getDecryptedToken(modrinthTestToken);
        }

        public void updateModrinthTestToken(String token) {
            this.modrinthTestToken = encryptToken(token);
        }

        public String getCurseforgeToken() {
            return getDecryptedToken(curseforgeToken);
        }

        public void updateCurseforgeToken(String token) {
            this.curseforgeToken = encryptToken(token);
        }

        public String getCurseforgeStudioToken() {
            return getDecryptedToken(curseforgeStudioToken);
        }

        public void updateCurseforgeStudioToken(String token) {
            this.curseforgeStudioToken = encryptToken(token);
        }

        public String getGithubToken() {
            return getDecryptedToken(githubToken);
        }

        public void updateGithubToken(String token) {
            this.githubToken = encryptToken(token);
        }

        public String getGitlabToken() {
            return getDecryptedToken(gitlabToken);
        }

        public void updateGitlabToken(String token) {
            this.gitlabToken = encryptToken(token);
        }
    }

}