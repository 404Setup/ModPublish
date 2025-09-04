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
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import lombok.Getter;
import one.pkg.modpublish.ui.base.BaseDialogWrapper;
import one.pkg.modpublish.util.protect.HardwareFingerprint;
import one.pkg.modpublish.util.protect.Protect;
import one.pkg.modpublish.util.resources.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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
    private JBTextField gitlabTokenText;

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
        addPlatformSection(formBuilder, "Gitlab", "/icons/gitlab.svg",
                new FieldConfig("Token", () -> gitlabTokenText = createTextField()));

        gitlabTokenText.setEnabled(false);
        gitlabTokenText.setToolTipText(Lang.get("tooltip.gitlab.disable"));

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

    @NotNull
    public String getGitlabTokenText() {
        return Protect.decryptString(gitlabTokenText.getText(), HardwareFingerprint.generateSecureProjectKey());
    }

    public void setGitlabTokenText(@NotNull String newText) {
        gitlabTokenText.setText(newText);
    }
}
