/*
 * Copyright (C) 2025 404Setup (https://github.com/404Setup)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package one.pkg.modpublish.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import one.pkg.modpublish.data.internel.Info;
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

        public Info getModrinthToken() {
            return getDecryptedToken(modrinthToken);
        }

        public void updateModrinthToken(String token) {
            this.modrinthToken = encryptToken(token);
        }

        public Info getModrinthTestToken() {
            return getDecryptedToken(modrinthTestToken);
        }

        public void updateModrinthTestToken(String token) {
            this.modrinthTestToken = encryptToken(token);
        }

        public Info getCurseforgeToken() {
            return getDecryptedToken(curseforgeToken);
        }

        public void updateCurseforgeToken(String token) {
            this.curseforgeToken = encryptToken(token);
        }

        public Info getCurseforgeStudioToken() {
            return getDecryptedToken(curseforgeStudioToken);
        }

        public void updateCurseforgeStudioToken(String token) {
            this.curseforgeStudioToken = encryptToken(token);
        }

        public Info getGithubToken() {
            return getDecryptedToken(githubToken);
        }

        public void updateGithubToken(String token) {
            this.githubToken = encryptToken(token);
        }

        public Info getGitlabToken() {
            return getDecryptedToken(gitlabToken);
        }

        public void updateGitlabToken(String token) {
            this.gitlabToken = encryptToken(token);
        }
    }

}