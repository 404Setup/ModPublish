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
import one.pkg.modpublish.data.internal.Info;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.net.Proxy;

// Don't migrate it to Kotlin
@State(
        name = "org.intellij.sdk.settings.AppSettings",
        storages = @Storage("ModPublish.xml")
)
public final class ModPublishSettings
        implements PersistentStateComponent<ModPublishSettings.State> {

    private State myState = new State();

    @NotNull
    public static ModPublishSettings getInstance() {
        return ApplicationManager.getApplication()
                .getService(ModPublishSettings.class);
    }

    @Override
    @NotNull
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
        public String curseforgeToken = "";
        @NonNls
        public String curseforgeStudioToken = "";
        @NonNls
        public String githubToken = "";
        @NonNls
        public String gitlabToken = "";
        @NonNls
        public boolean autoProxy = false;
        @NonNls
        public int proxyType = 0; // 0: sockets; 1: http
        @NonNls
        public String proxyAddress = "";
        @NonNls
        public int proxyPort = 3366;
        @NonNls
        public String proxyUsername = "";
        @NonNls
        public String proxyPassword = "";
        @NonNls
        public boolean networkEnableSSLCheck = true;
        @NonNls
        public int networkConnectTimeout = 20;
        @NonNls
        public int networkReadTimeout = 20;
        @NonNls
        public int networkWriteTimeout = 20;

        public void updateNetworkConnectTimeout(int timeout) {
            if (timeout > 0 && timeout < Integer.MAX_VALUE)
                networkConnectTimeout = timeout;
        }

        public void updateNetworkReadTimeout(int timeout) {
            if (timeout > 0 && timeout < Integer.MAX_VALUE)
                networkReadTimeout = timeout;
        }

        public void updateNetworkWriteTimeout(int timeout) {
            if (timeout > 0 && timeout < Integer.MAX_VALUE)
                networkWriteTimeout = timeout;
        }

        public Proxy.Type getProxyType() {
            return proxyType == 0 ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
        }

        public Info getModrinthToken() {
            return getDecryptedToken(modrinthToken);
        }

        public void updateModrinthToken(String token) {
            this.modrinthToken = encryptToken(token);
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