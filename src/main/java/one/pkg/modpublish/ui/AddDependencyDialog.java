package one.pkg.modpublish.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import one.pkg.modpublish.api.API;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.TargetType;
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.DependencyType;
import one.pkg.modpublish.ui.base.BaseDialogWrapper;

import javax.swing.*;
import java.awt.*;

public class AddDependencyDialog extends BaseDialogWrapper {
    private final boolean[] publishTargets; // [github, gitlab, modrinth, modrinthTest, curseforge]
    private final Project project;
    private JBTextField projectIdField;
    private JComboBox<DependencyType> dependencyTypeCombo;
    private boolean isDone = false;
    private DependencyInfo resultDependency;

    public AddDependencyDialog(PublishModDialog parent, boolean[] publishTargets) {
        super(parent.getProject(), true);
        this.project = parent.getProject();
        this.publishTargets = publishTargets;
        setText("title.add-dependency", TextType.Title);
        init();
    }

    public boolean isDone() {
        return isDone;
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;

        // Project ID field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(getJBLabel("component.name.depend-id"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        projectIdField = new JBTextField(30);
        panel.add(projectIdField, gbc);

        // Help text
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JBLabel helpLabel = new JBLabel("<html><small>" + get("tips.1") + "</small></html>");
        helpLabel.setForeground(JBColor.GRAY);
        panel.add(helpLabel, gbc);

        // Dependency type
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(getJBLabel("component.name-depend-status"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        dependencyTypeCombo = new ComboBox<>(DependencyType.values());
        panel.add(dependencyTypeCombo, gbc);

        return panel;
    }

    @Override
    protected void doOKAction() {
        if (!publishTargets[2] && !publishTargets[3] && !publishTargets[4]) {
            if (publishTargets[0] || publishTargets[1])
                showMessageDialog("message.dont-support-add-depends", "title.failed", JOptionPane.ERROR_MESSAGE);
            else showMessageDialog("failed.8", "title.failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String projectId = projectIdField.getText().trim();
        if (projectId.isEmpty()) {
            showMessageDialog(
                    "failed.9", "title.failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DependencyType selectedType =
                (DependencyType) dependencyTypeCombo.getSelectedItem();

        resultDependency = new DependencyInfo(projectId, selectedType, null);

        // Validate the dependency
        ModInfo[] validationResult = validateDependency(resultDependency);
        if (validationResult.length == 1) {
            ModInfo i = validationResult[0];
            if (i.failed() != null) {
                JOptionPane.showMessageDialog(getContentPanel(),
                        validationResult[0].failed(),
                        get("title.failed"),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (validationResult[0] != null) {
            resultDependency.setCustomTitle(validationResult[0].name());
            resultDependency.setModrinthInfo(validationResult[0]);
        }
        if (validationResult[1] != null) {
            resultDependency.setCustomTitle(validationResult[1].name());
            resultDependency.setCurseforgeInfo(validationResult[1]);
        }
        isDone = true;
        super.doOKAction();
    }

    private ModInfo[] validateDependency(DependencyInfo dependency) {
        String projectId = dependency.getProjectId();

        if (projectId == null || projectId.trim().isEmpty()) {
            return ModInfo.ofs("Project ID cannot be empty");
        }

        if (projectId.contains(",")) {
            String[] parts = projectId.split(",", 2);
            if (parts.length != 2/* || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()*/) {
                return ModInfo.ofs("Invalid project ID format");
            }
            ModInfo[] infos = new ModInfo[]{null, null};
            if (publishTargets[2] && !parts[0].trim().isEmpty()) {
                ModInfo modInfo = TargetType.Modrinth.getApi().getModInfo(parts[0], project);
                if (modInfo.failed() != null) return ModInfo.of(modInfo);
                infos[0] = modInfo;
            }
            if (publishTargets[3] && !parts[1].trim().isEmpty()) {
                ModInfo modInfo = TargetType.CurseForge.getApi().getModInfo(parts[1], project);
                if (modInfo.failed() != null) return ModInfo.of(modInfo);
                infos[1] = modInfo;
            }
            return infos;
        }

        ModInfo[] infos = new ModInfo[2];
        API modrinthApi = TargetType.Modrinth.getApi();
        API curseforgeApi = TargetType.CurseForge.getApi();
        if (publishTargets[2]) { // Modrinth
            if (modrinthApi.getABServer()) modrinthApi.updateABServer();
            ModInfo modInfo = modrinthApi.getModInfo(projectId, project);
            if (modInfo.failed() != null) return ModInfo.of(modInfo);
            infos[0] = modInfo;
        }
        if (publishTargets[3]) {// Modrinth Test
            if (!modrinthApi.getABServer()) modrinthApi.updateABServer();
            ModInfo modInfo = modrinthApi.getModInfo(projectId, project);
            if (modInfo.failed() != null) return ModInfo.of(modInfo);
            infos[0] = modInfo;
        }
        if (publishTargets[4]) { // CurseForge
            ModInfo modInfo = curseforgeApi.getModInfo(projectId, project);
            if (modInfo.failed() != null) return ModInfo.of(modInfo);
            infos[1] = modInfo;
        }
        return infos;
    }

    public DependencyInfo getDependency() {
        return resultDependency;
    }
}