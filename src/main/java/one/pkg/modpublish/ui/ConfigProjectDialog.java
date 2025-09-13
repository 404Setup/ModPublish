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

package one.pkg.modpublish.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import one.pkg.modpublish.settings.properties.PID;
import one.pkg.modpublish.settings.properties.Properties;
import one.pkg.modpublish.settings.properties.Property;
import one.pkg.modpublish.ui.base.BaseDialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigProjectDialog extends BaseDialogWrapper {
    private final Project project;

    private JBTextField commonVersionFormatField;

    private JBTextField modrinthTokenField;
    private JBTextField modrinthTestTokenField;
    private JBTextField modrinthModIDField;
    private JBTextField modrinthTestModIDField;

    private JBTextField curseforgeTokenField;
    private JBTextField curseforgeStudioTokenField;
    private JBTextField curseforgeModIDField;

    private JBTextField githubTokenField;
    private JBTextField githubRepoField;
    private JBTextField githubBranchField;

    public ConfigProjectDialog(Project project) {
        super(project);
        this.project = project;

        setTitle("title.config-project", project.getName());
        setModal(true);

        init();
        setText("button.save", TextType.OKButton);
        setText("button.cancel", TextType.CancelButton);
        //getContentPanel().setPreferredSize(new Dimension(500, 400));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        String token = get("dialog.modpublish.config-project.token");
        String studioToken = get("dialog.modpublish.config-project.studio-token");
        String modIdLabel = "Mod ID:";
        String repoLabel = get("dialog.modpublish.config-project.repo");
        String branchLabel = get("dialog.modpublish.config-project.branch");

        addPlatformSection(formBuilder, "Common", "/icons/book.svg",
                FieldConfig.of(get("component.name.common.version-format"), () -> commonVersionFormatField = createTextField()));

        addPlatformSection(formBuilder, "Modrinth", "/icons/modrinth.svg",
                FieldConfig.of(token, () -> modrinthTokenField = createTextField()),
                FieldConfig.of("(Test) " + token, () -> modrinthTestTokenField = createTextField()),
                FieldConfig.of(modIdLabel, () -> modrinthModIDField = createTextField()),
                FieldConfig.of("(Test) " + modIdLabel, () -> modrinthTestModIDField = createTextField()));

        addPlatformSection(formBuilder, "CurseForge", "/icons/curseforge.svg",
                FieldConfig.of(token, () -> curseforgeTokenField = createTextField()),
                FieldConfig.of(studioToken, () -> curseforgeStudioTokenField = createTextField()),
                FieldConfig.of(modIdLabel, () -> curseforgeModIDField = createTextField()));

        addPlatformSection(formBuilder, "GitHub", "/icons/github.svg",
                FieldConfig.of(token, () -> githubTokenField = createTextField()),
                FieldConfig.of(repoLabel, () -> {
                    githubRepoField = createTextField();
                    githubRepoField.setToolTipText(get("dialog.modpublish.config-project.repo.tooltips"));
                    return githubRepoField;
                }),
                FieldConfig.of(branchLabel, () -> {
                    githubBranchField = createTextField();
                    githubBranchField.setToolTipText(get("dialog.modpublish.config-project.branch.tooltips"));
                    return githubBranchField;
                }));

        autoFillFields();

        return toScrollPanel(formBuilder, 600, 360);
    }

    @Override
    protected void doOKAction() {
        savePersistedData();
        close(OK_EXIT_CODE);
    }

    private void autoFillFields() {
        loadPersistedData();
    }

    private void loadPersistedData() {
        Property p1 = Properties.getProperties(project);

        commonVersionFormatField.setText(p1.common().versionFormat());

        if (p1.modrinth().token().globalData())
            setToolTipText("dialog.modpublish.config-project.global", modrinthTokenField);
        else modrinthTokenField.setText(p1.modrinth().token().data());
        if (p1.modrinth().testToken().globalData())
            setToolTipText("dialog.modpublish.config-project.global", modrinthTestTokenField);
        else modrinthTestTokenField.setText(p1.modrinth().testToken().data());
        modrinthModIDField.setText(p1.modrinth().modid());
        modrinthTestModIDField.setText(p1.modrinth().testModId());

        if (p1.curseforge().token().globalData()) {
            setToolTipText("dialog.modpublish.config-project.global", curseforgeTokenField);
        } else curseforgeTokenField.setText(p1.curseforge().token().data());
        if (p1.curseforge().studioToken().globalData()) {
            setToolTipText("dialog.modpublish.config-project.global", curseforgeStudioTokenField);
        } else curseforgeStudioTokenField.setText(p1.curseforge().studioToken().data());
        curseforgeModIDField.setText(p1.curseforge().modid());

        githubTokenField.setText(p1.github().token().globalData() ? "" : p1.github().token().data());
        if (p1.github().token().globalData())
            setToolTipText("dialog.modpublish.config-project.global", githubTokenField);
        githubRepoField.setText(p1.github().repo());
        githubBranchField.setText(p1.github().branch());
    }

    private void savePersistedData() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);

        PID.CommonVersionFormat.set(properties, commonVersionFormatField);

        PID.ModrinthToken.set(properties, modrinthTokenField);
        PID.ModrinthTestToken.set(properties, modrinthTestTokenField);
        PID.ModrinthModID.set(properties, modrinthModIDField);
        PID.ModrinthTestModID.set(properties, modrinthTestModIDField);

        PID.CurseForgeToken.set(properties, curseforgeTokenField);
        PID.CurseForgeStudioToken.set(properties, curseforgeStudioTokenField);
        PID.CurseForgeModID.set(properties, curseforgeModIDField);

        PID.GithubToken.set(properties, githubTokenField);
        PID.GithubRepo.set(properties, githubRepoField);
        PID.GithubBranch.set(properties, githubBranchField);
    }
}
