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

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class ModPublishSettingsConfigurable implements Configurable {

    private ModPublishSettingsComponent component;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "ModPublish: Global Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        component = new ModPublishSettingsComponent();
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        ModPublishSettings.State state =
                Objects.requireNonNull(ModPublishSettings.getInstance().getState());
        return !component.getModrinthTokenText().equals(state.getModrinthToken().data()) ||
                !component.getModrinthTestTokenText().equals(state.getModrinthTestToken().data()) ||
                !component.getCurseforgeTokenText().equals(state.getCurseforgeToken().data()) ||
                !component.getCurseforgeStudioTokenText().equals(state.getCurseforgeStudioToken().data()) ||
                !component.getGithubTokenText().equals(state.getGithubToken().data()) ||
                component.isAutoProxyEnabled() != state.autoProxy ||
                component.getProxyType() != state.proxyType ||
                !component.getProxyAddress().equals(state.proxyAddress) ||
                component.getProxyPort() != state.proxyPort;
    }

    @Override
    public void apply() {
        ModPublishSettings.State state =
                Objects.requireNonNull(ModPublishSettings.getInstance().getState());
        state.updateModrinthToken(component.getModrinthTokenText());
        state.updateModrinthTestToken(component.getModrinthTestTokenText());
        state.updateCurseforgeToken(component.getCurseforgeTokenText());
        state.updateCurseforgeStudioToken(component.getCurseforgeStudioTokenText());
        state.updateGithubToken(component.getGithubTokenText());
        state.autoProxy = component.isAutoProxyEnabled();
        state.proxyType = component.getProxyType();
        state.proxyAddress = component.getProxyAddress();
        state.proxyPort = component.getProxyPort();
    }

    @Override
    public void reset() {
        ModPublishSettings.State state =
                Objects.requireNonNull(ModPublishSettings.getInstance().getState());
        component.setModrinthTokenText(state.getModrinthToken().data());
        component.setModrinthTestTokenText(state.getModrinthTestToken().data());
        component.setCurseforgeTokenText(state.getCurseforgeToken().data());
        component.setCurseforgeStudioTokenText(state.getCurseforgeStudioToken().data());
        component.setGithubTokenText(state.getGithubToken().data());
        component.setAutoProxyEnabled(state.autoProxy);
        component.setProxyType(state.proxyType);
        component.setProxyAddress(state.proxyAddress);
        component.setProxyPort(state.proxyPort);
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

}