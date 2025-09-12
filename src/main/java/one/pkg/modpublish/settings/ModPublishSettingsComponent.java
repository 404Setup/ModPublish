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

import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import lombok.Getter;
import one.pkg.modpublish.ui.base.BaseDialogWrapper;
import one.pkg.modpublish.util.protect.HardwareFingerprint;
import one.pkg.modpublish.util.protect.Protect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.Proxy;

@SuppressWarnings("unused")
public class ModPublishSettingsComponent extends BaseDialogWrapper {
    @Getter
    private final JPanel panel;

    private JBTextField modrinthTokenText;
    private JBTextField modrinthTestTokenText;
    private ActionLink modrinthTokenLink;
    private ActionLink modrinthTestTokenLink;

    private JBTextField curseforgeTokenText;
    private JBTextField curseforgeStudioTokenText;
    private ActionLink curseforgeTokenLink;
    private ActionLink curseforgeStudioTokenLink;

    private JBTextField githubTokenText;
    private ActionLink githubTokenLink;

    private JBCheckBox autoProxyCheckBox;
    private JComboBox<Proxy.Type> proxyTypeComboBox;
    private JBTextField proxyAddressText;
    private JBTextField proxyPortText;

    private JBCheckBox networkEnableSSLCheckBox;
    private JBTextField networkConnectTimeoutText;
    private JBTextField networkReadTimeoutText;
    private JBTextField networkWriteTimeoutText;

    public ModPublishSettingsComponent() {
        super(null);
        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        addPlatformSection(formBuilder, "Modrinth", "/icons/modrinth.svg",
                new FieldConfig("Token", () -> modrinthTokenText = createTextField()),
                new FieldConfig("(Test) Token", () -> modrinthTestTokenText = createTextField()),
                new FieldConfig(() -> modrinthTokenLink = createActionLink("Create Modrinth Token", "https://modrinth.com/settings/pats")),
                new FieldConfig(() -> modrinthTestTokenLink = createActionLink("Create Modrinth Test Token", "https://staging.modrinth.com/settings/pats")));

        addPlatformSection(formBuilder, "CurseForge", "/icons/curseforge.svg",
                new FieldConfig("Token", () -> curseforgeTokenText = createTextField()),
                new FieldConfig("Studio Token", () -> curseforgeStudioTokenText = createTextField()),
                new FieldConfig(() -> curseforgeStudioTokenLink = createActionLink("Create CurseForge Studio Token", "https://console.curseforge.com/?#/api-keys")),
                new FieldConfig(() -> curseforgeTokenLink = createActionLink("Create CurseForge Token", "https://legacy.curseforge.com/account/api-tokens")));

        addPlatformSection(formBuilder, "GitHub", "/icons/github.svg",
                new FieldConfig("Token", () -> githubTokenText = createTextField()),
                new FieldConfig(() -> githubTokenLink = createActionLink("Create GitHub Token", "https://github.com/settings/personal-access-tokens")));

        addPlatformSection(formBuilder, get("setting.network.name"), "/icons/databar.svg",
                new FieldConfig(get("setting.network.ssl-check.name"), () -> networkEnableSSLCheckBox = new JBCheckBox()),
                new FieldConfig(() -> createLabel(get("setting.network.ssl-check.desc"))),
                new FieldConfig(get("setting.network.read-timeout.name"), () -> {
                    networkReadTimeoutText = createSimpleNumericTextField(1, Integer.MAX_VALUE);
                    networkReadTimeoutText.setToolTipText(get("setting.network.read-timeout.desc"));
                    return networkReadTimeoutText;
                }),
                new FieldConfig(get("setting.network.write-timeout.name"), () -> {
                    networkWriteTimeoutText = createSimpleNumericTextField(1, Integer.MAX_VALUE);
                    networkWriteTimeoutText.setToolTipText(get("setting.network.write-timeout.desc"));
                    return networkWriteTimeoutText;
                }),
                new FieldConfig(get("setting.network.connect-timeout.name"), () -> {
                    networkConnectTimeoutText = createSimpleNumericTextField(1, Integer.MAX_VALUE);
                    networkConnectTimeoutText.setToolTipText(get("setting.network.connect-timeout.desc"));
                    return networkConnectTimeoutText;
                })
        );

        addPlatformSection(formBuilder, get("setting.proxy.name"), "/icons/globe.svg",
                new FieldConfig(() -> createLabel(get("tips.3"))),
                new FieldConfig(get("setting.proxy.auto-proxy"), () -> autoProxyCheckBox = new JBCheckBox()),
                new FieldConfig(get("setting.proxy.type"), () -> proxyTypeComboBox = new JComboBox<>(new Proxy.Type[]{Proxy.Type.SOCKS, Proxy.Type.HTTP})),
                new FieldConfig(get("setting.proxy.address"), () -> proxyAddressText = createTextField()),
                new FieldConfig(get("setting.proxy.port"), () -> proxyPortText = createSimpleNumericTextField(1, 65535)));

        panel = formBuilder.addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return null;
    }

    public JComponent getPreferredFocusedComponent() {
        return modrinthTokenText;
    }

    @NotNull
    public String getModrinthTokenText() {
        return Protect.decryptString(modrinthTokenText.getText(), HardwareFingerprint.generateSecureProjectKey());
    }

    public void setModrinthTokenText(@NotNull String newText) {
        modrinthTokenText.setText(newText);
    }

    @NotNull
    public String getModrinthTestTokenText() {
        return Protect.decryptString(modrinthTestTokenText.getText(), HardwareFingerprint.generateSecureProjectKey());
    }

    public void setModrinthTestTokenText(@NotNull String newText) {
        modrinthTestTokenText.setText(newText);
    }

    @NotNull
    public String getCurseforgeTokenText() {
        return Protect.decryptString(curseforgeTokenText.getText(), HardwareFingerprint.generateSecureProjectKey());
    }

    public void setCurseforgeTokenText(@NotNull String newText) {
        curseforgeTokenText.setText(newText);
    }

    @NotNull
    public String getCurseforgeStudioTokenText() {
        return Protect.decryptString(curseforgeStudioTokenText.getText(), HardwareFingerprint.generateSecureProjectKey());
    }

    public void setCurseforgeStudioTokenText(@NotNull String newText) {
        curseforgeStudioTokenText.setText(newText);
    }

    @NotNull
    public String getGithubTokenText() {
        return Protect.decryptString(githubTokenText.getText(), HardwareFingerprint.generateSecureProjectKey());
    }

    public void setGithubTokenText(@NotNull String newText) {
        githubTokenText.setText(newText);
    }

    public boolean isAutoProxyEnabled() {
        return autoProxyCheckBox.isSelected();
    }

    public void setAutoProxyEnabled(boolean enabled) {
        autoProxyCheckBox.setSelected(enabled);
    }

    public int getProxyType() {
        return proxyTypeComboBox.getSelectedIndex();
    }

    public void setProxyType(@NotNull Proxy.Type newType) {
        proxyTypeComboBox.setSelectedItem(newType);
    }

    public void setProxyType(int newType) {
        proxyTypeComboBox.setSelectedItem(newType);
    }

    public String getProxyAddress() {
        return proxyAddressText.getText();
    }

    public void setProxyAddress(@NotNull String newAddress) {
        proxyAddressText.setText(newAddress);
    }

    public int getProxyPort() {
        return Integer.parseInt(proxyPortText.getText());
    }

    public void setProxyPort(int newPort) {
        proxyPortText.setText(String.valueOf(newPort));
    }

    public boolean isNetworkEnableSSLCheck() {
        return networkEnableSSLCheckBox.isSelected();
    }

    public void setNetworkEnableSSLCheck(boolean enabled) {
        networkEnableSSLCheckBox.setSelected(enabled);
    }

    public int getNetworkConnectTimeout() {
        return Integer.parseInt(networkConnectTimeoutText.getText());
    }

    public void setNetworkConnectTimeout(int newTimeout) {
        networkConnectTimeoutText.setText(String.valueOf(newTimeout));
    }

    public int getNetworkReadTimeout() {
        return Integer.parseInt(networkReadTimeoutText.getText());
    }

    public void setNetworkReadTimeout(int newTimeout) {
        networkReadTimeoutText.setText(String.valueOf(newTimeout));
    }

    public int getNetworkWriteTimeout() {
        return Integer.parseInt(networkWriteTimeoutText.getText());
    }

    public void setNetworkWriteTimeout(int newTimeout) {
        networkWriteTimeoutText.setText(String.valueOf(newTimeout));
    }
}
