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
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import lombok.Getter;
import one.pkg.modpublish.PluginMain;
import one.pkg.modpublish.api.API;
import one.pkg.modpublish.data.internel.*;
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.local.SupportedInfo;
import one.pkg.modpublish.data.result.PublishResult;
import one.pkg.modpublish.settings.properties.PID;
import one.pkg.modpublish.settings.properties.Properties;
import one.pkg.modpublish.settings.properties.Property;
import one.pkg.modpublish.ui.base.BaseDialogWrapper;
import one.pkg.modpublish.ui.icon.Icons;
import one.pkg.modpublish.ui.panel.DependencyManagerPanel;
import one.pkg.modpublish.ui.renderer.CheckBoxListCellRenderer;
import one.pkg.modpublish.util.io.Async;
import one.pkg.modpublish.util.io.FileAPI;
import one.pkg.modpublish.util.io.JsonParser;
import one.pkg.modpublish.util.io.VersionProcessor;
import one.pkg.modpublish.util.resources.Lang;
import one.pkg.modpublish.util.resources.LocalResources;
import one.pkg.modpublish.util.version.constraint.VersionConstraint;
import one.pkg.modpublish.util.version.constraint.VersionConstraintParser;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class PublishModDialog extends BaseDialogWrapper {
    @Getter
    private final Project project;
    @NotNull
    private final Map<VirtualFile, List<ModType>> modTypes;
    private VirtualFile[] jarFile;
    private LocalModInfo modInfo;
    private VersionConstraint parser;
    private boolean updateVersionList;

    // UI Components
    private JBTextField versionNameField;
    private JBTextField versionNumberField;

    // Publish targets
    private JBCheckBox githubCheckBox;
    private JBCheckBox modrinthCheckBox;
    private JBCheckBox curseforgeCheckBox;

    // Support targets
    private JBCheckBox clientCheckBox;
    private JBCheckBox serverCheckBox;
    private JComboBox<ReleaseChannel> releaseType;
    private JComboBox<VirtualFile> primaryFile;

    // Loaders
    private List<JBCheckBox> loaderCheckBoxes;

    // Minecraft versions
    private JList<MinecraftVersionItem> minecraftVersionList;
    private DefaultListModel<MinecraftVersionItem> minecraftVersionModel;
    private JBCheckBox showSnapshotsCheckBox;

    // Changelog and dependencies
    private EditorTextField changelogField;
    private DependencyManagerPanel dependencyPanel;

    // Data
    private List<MinecraftVersion> minecraftVersions = null;
    private List<LauncherInfo> launchers;
    private SupportedInfo supportedInfo;

    public PublishModDialog(@Nullable Project project, VirtualFile... jarFile) {
        super(project);
        this.project = project;
        this.jarFile = jarFile;
        this.modTypes = new HashMap<>();
        for (VirtualFile file : jarFile) {
            List<ModType> types = ModType.getAll(file);
            this.modTypes.put(file, types);
        }

        updateParser(jarFile[0]);

        PluginMain.updateProject(project);

        setTitle("title.publish", jarFile[0].getName());
        setModal(true);

        loadConfigData();
        init();
        setText("button.publish", TextType.OKButton);
        setText("button.cancel", TextType.CancelButton);
        setOKButtonDefault();
    }

    private void updateParser(@NotNull VirtualFile primaryFile) {
        List<ModType> type = this.modTypes.get(primaryFile);
        if (type != null) {
            for (ModType modType : type) {
                if (modType.equals(ModType.Rift)) continue;
                this.modInfo = modType.getMod(primaryFile);
                if (this.modInfo != null) {
                    this.parser = modInfo.versionRange() != null && !modInfo.versionRange().isEmpty() ?
                            VersionConstraintParser.parse(modInfo.versionRange()) :
                            null;
                    break;
                }
            }
        }
    }

    private void loadConfigData() {
        try {
            launchers = LocalResources.getLauncherInfo();
            supportedInfo = LocalResources.getSupportedInfo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateJarFiles(@NotNull VirtualFile current) {
        VirtualFile[] newJarFiles = new VirtualFile[jarFile.length];
        newJarFiles[0] = current;
        int index = 1;
        for (VirtualFile file : jarFile) {
            if (!file.equals(current)) {
                newJarFiles[index++] = file;
            }
        }
        this.jarFile = newJarFiles;
    }

    private void onPrimaryFileUpdate() {
        VirtualFile current = primaryFile.getItemAt(primaryFile.getSelectedIndex());
        updateJarFiles(current);

        List<ModType> types = this.modTypes.get(current);
        for (JBCheckBox checkBox : loaderCheckBoxes) {
            checkBox.setSelected(types.contains(ModType.of(checkBox.getText().toLowerCase())));
        }
        updateParser(current);
        loadModInfo(current);
        updateMinecraftVersions();
        setTitle("title.publish", current.getName());
    }

    @Override
    protected @NotNull JComponent createCenterPanel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        // Version name and number
        versionNameField = new JBTextField();
        versionNumberField = new JBTextField();

        formBuilder.addLabeledComponent(Lang.get("component.name.version-name"), versionNameField);
        formBuilder.addLabeledComponent(Lang.get("component.name.version-number"), versionNumberField);

        // Publish targets
        githubCheckBox = getJBCheckBoxRaw("GitHub");
        modrinthCheckBox = getJBCheckBoxRaw("Modrinth");
        curseforgeCheckBox = getJBCheckBoxRaw("CurseForge");

        JPanel publishTargetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        publishTargetsPanel.add(githubCheckBox);
        publishTargetsPanel.add(modrinthCheckBox);
        publishTargetsPanel.add(curseforgeCheckBox);
        formBuilder.addLabeledComponent(Lang.get("component.name.targets"), publishTargetsPanel);

        // Support targets
        clientCheckBox = getJBCheckBox("dialog.modpublish.publish.support.client");
        serverCheckBox = getJBCheckBox("dialog.modpublish.publish.support.server");

        JPanel supportTargetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        supportTargetsPanel.add(clientCheckBox);
        supportTargetsPanel.add(serverCheckBox);
        formBuilder.addLabeledComponent(Lang.get("dialog.modpublish.publish.support.title"), supportTargetsPanel);

        // Loaders
        loaderCheckBoxes = new ArrayList<>();
        JPanel loadersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        List<ModType> pTargets = modTypes.values().iterator().next();

        for (LauncherInfo launcher : launchers) {
            JBCheckBox checkBox = getJBCheckBoxRaw(launcher.n);
            if (pTargets.contains(ModType.of(launcher.n.toLowerCase())))
                checkBox.setSelected(true);
            loaderCheckBoxes.add(checkBox);
            loadersPanel.add(checkBox);
        }
        formBuilder.addLabeledComponent(get("component.name.loaders"), loadersPanel);

        primaryFile = new ComboBox<>(jarFile);
        primaryFile.addActionListener(e -> onPrimaryFileUpdate());
        primaryFile.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof VirtualFile) {
                    setText(((VirtualFile) value).getName());
                }
                return this;
            }
        });

        JPanel primaryFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        primaryFilePanel.add(primaryFile);
        formBuilder.addLabeledComponent(get("component.name.primary-file"), primaryFilePanel);

        JPanel releaseTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        releaseType = new ComboBox<>(ReleaseChannel.values());
        releaseTypePanel.add(releaseType);
        formBuilder.addLabeledComponent(get("component.name.release-channel"), releaseTypePanel);

        // Minecraft versions
        minecraftVersionModel = new DefaultListModel<>();
        minecraftVersionList = new JBList<>(minecraftVersionModel);
        minecraftVersionList.setCellRenderer(new CheckBoxListCellRenderer());
        minecraftVersionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        minecraftVersionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = minecraftVersionList.locationToIndex(e.getPoint());
                if (index != -1) {
                    MinecraftVersionItem item = minecraftVersionModel.getElementAt(index);
                    item.setSelected(!item.isSelected());
                    minecraftVersionList.repaint();
                }
            }
        });

        showSnapshotsCheckBox = getJBCheckBox("component.name.snapshot");

        updateMinecraftVersions();

        showSnapshotsCheckBox.addActionListener(e -> updateMinecraftVersions());

        JBScrollPane minecraftScrollPane = new JBScrollPane(minecraftVersionList);
        minecraftScrollPane.setPreferredSize(new Dimension(200, 120));

        addPlatformSection(formBuilder, get("component.name.mc-version"), Icons.Static.ListBar,
                FieldConfig.of(() -> {
                    JPanel minecraftPanel = new JPanel(new BorderLayout());
                    minecraftPanel.add(minecraftScrollPane, BorderLayout.CENTER);
                    minecraftPanel.add(showSnapshotsCheckBox, BorderLayout.SOUTH);
                    return minecraftPanel;
                }),
                FieldConfig.of((jButton) -> {
                    jButton.setText(get("component.name.update-version-list"));
                    jButton.setIcon(Icons.Static.Sync);
                    jButton.setToolTipText(get("component.tooltip.update-version-list"));
                    jButton.addActionListener(e -> Async.runAsync(() -> {
                        jButton.setEnabled(false);
                        setButtonLoading(jButton);
                        if (VersionProcessor.updateVersions()) {
                            updateVersionList = true;
                            showSuccessDialog("message.update.success", "title.success");
                            updateMinecraftVersions();
                        } else {
                            showFailedDialog("message.update.failed", "title.failed");
                        }
                        jButton.setIcon(Icons.Static.Sync);
                        jButton.setEnabled(true);
                    }));
                }),
                FieldConfig.of((jButton) -> {
                    jButton.setText(get("component.name.reset-version-list"));
                    jButton.setIcon(Icons.Static.WrenchScrewdriver);
                    jButton.setToolTipText(get("component.tooltip.reset-version-list"));
                    jButton.addActionListener(e -> Async.runAsync(() -> {
                        File f = FileAPI.getUserDataFile("minecraft.version.json");
                        if (f.exists()) f.delete();
                        showSuccessDialog("message.update.success", "title.success");
                        updateVersionList = true;
                        updateMinecraftVersions();
                    }));
                }));

        // Changelog
        addPlatformSection(formBuilder, Lang.get("component.name.changelog"), Icons.Static.Clipboard,
                FieldConfig.of(() -> {
                    try {
                        MarkdownFileType markdownFileType = MarkdownFileType.INSTANCE;
                        changelogField = new EditorTextField("", project, markdownFileType);
                    } catch (Exception e) {
                        changelogField = new EditorTextField("", project, PlainTextFileType.INSTANCE);
                    }

                    changelogField.setPreferredSize(new Dimension(500, 150));
                    changelogField.setMinimumSize(new Dimension(500, 100));
                    changelogField.setOneLineMode(false);
                    return changelogField;
                }));

        // Dependency manager
        addPlatformSection(formBuilder, Lang.get("component.name.dependencies"), Icons.Static.Library,
                FieldConfig.of(() -> dependencyPanel = new DependencyManagerPanel(this)));

        autoFillFields();

        return toScrollPanel(formBuilder, 800, 650);
    }

    private void updateMinecraftVersions() {
        SwingUtilities.invokeLater(() -> {
            minecraftVersionModel.clear();
            boolean includeSnapshots = showSnapshotsCheckBox.isSelected();

            if (minecraftVersions == null || updateVersionList) {
                updateVersionList = false;
                minecraftVersions = LocalResources.getMinecraftVersions();
            }

            for (MinecraftVersion version : minecraftVersions) {
                if ("release".equals(version.type) || (includeSnapshots && "snapshot".equals(version.type))) {
                    minecraftVersionModel.addElement(new MinecraftVersionItem(version, false));
                }
            }

            autoFillMinecraftVersions();
        });
    }

    private void autoFillFields() {
        loadModInfo(this.jarFile[0]);
        loadPersistedData();
    }

    private void loadModInfo(@NotNull VirtualFile current) {
        String version = null;
        String versionName = current.getNameWithoutExtension();
        String versionNameFormat = PID.CommonVersionFormat.get(project);
        ModType modType = modTypes.get(current).getFirst();

        if (modInfo != null) {
            version = modInfo.version();
            String lowVersion = parser.getLowVersion();
            String highVersion = parser.getMaxVersion();

            if (!versionNameFormat.isEmpty()) {
                versionName =
                        versionNameFormat.replace("{version}", Objects.requireNonNull(version))
                                .replace("{name}", Objects.requireNonNull(modInfo.name()))
                                .replace("{loader}", modType.getName());
                if (!lowVersion.isEmpty()) versionName = versionName.replace("{low-version}", lowVersion);
                if (!highVersion.isEmpty()) versionName = versionName.replace("{high-version}", highVersion);
            }
        }

        if (version == null) version = ModVersion.extractVersionNumber(current);

        versionNameField.setText(versionName);
        versionNumberField.setText(version);
    }

    private void autoFillMinecraftVersions() {
        if (minecraftVersionModel.getSize() > 0) {
            if (parser != null) {
                String lowVersion = parser.getLowVersion();
                String highVersion = parser.getMaxVersion();

                // Find indices for low and high versions
                int lowIndex = -1;
                int highIndex = -1;

                for (int i = 0; i < minecraftVersionModel.getSize(); i++) {
                    MinecraftVersionItem item = minecraftVersionModel.getElementAt(i);
                    String version = item.getVersion().version;

                    if (!lowVersion.isBlank() && version.equals(lowVersion)) {
                        lowIndex = i;
                    }
                    if (!highVersion.isBlank() && version.equals(highVersion)) {
                        highIndex = i;
                    }
                }

                if (!lowVersion.isBlank() && !highVersion.isBlank() && lowIndex != -1 && highIndex != -1) {
                    int startIndex = Math.min(lowIndex, highIndex);
                    int endIndex = Math.max(lowIndex, highIndex);
                    for (int i = startIndex; i <= endIndex; i++) {
                        minecraftVersionModel.getElementAt(i).setSelected(true);
                    }
                } else if (!lowVersion.isBlank() && lowIndex != -1) {
                    // Select from lowVersion to index 0
                    for (int i = 0; i <= lowIndex; i++) {
                        minecraftVersionModel.getElementAt(i).setSelected(true);
                    }
                } else if (!highVersion.isBlank() && highIndex != -1) {
                    // Select only highVersion
                    minecraftVersionModel.getElementAt(highIndex).setSelected(true);
                }
            } else {
                // If no version constraints, select the latest version (index 0)
                minecraftVersionModel.getElementAt(0).setSelected(true);
            }
            minecraftVersionList.repaint();
        }
    }

    private void setFailedSelect(@NotNull JBCheckBox jbCheckBox) {
        jbCheckBox.setEnabled(false);
        setErrorStyle(jbCheckBox);
        setToolTipText("tooltip.decrypt.failed", jbCheckBox);
    }

    private void loadPersistedData() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);

        Property p2 = Properties.getProperties(properties);

        if (!p2.modrinth().isEnabled()) {
            modrinthCheckBox.setEnabled(false);
            setToolTipText("tooltip.modrinth.disable", modrinthCheckBox);
        } else if (p2.modrinth().token().failed())
            setFailedSelect(modrinthCheckBox);

        if (!p2.curseforge().isEnabled()) {
            curseforgeCheckBox.setEnabled(false);
            setToolTipText("tooltip.curseforge.disable", curseforgeCheckBox);
        } else if (p2.curseforge().token().failed() || p2.curseforge().studioToken().failed())
            setFailedSelect(curseforgeCheckBox);

        if (!p2.github().isEnabled()) {
            githubCheckBox.setEnabled(false);
            setToolTipText("tooltip.git.disable", githubCheckBox, "Github");
        } else if (p2.github().token().failed())
            setFailedSelect(githubCheckBox);

        // Load changelog
        String savedChangelog = properties.getValue("modpublish.changelog", "");
        changelogField.setText(savedChangelog);

        // Load dependencies
        String savedDependenciesJson = properties.getValue("modpublish.dependencies", "[]");
        try {
            List<DependencyInfo> savedDependencies = JsonParser.fromJson(savedDependenciesJson, LocalResources.dpType);
            if (savedDependencies != null) {
                dependencyPanel.setDependencies(savedDependencies);
            }
        } catch (Exception e) {
            // If loading fails, start with empty dependencies
            dependencyPanel.setDependencies(new ArrayList<>());
        }
    }

    /**
     * Save changelog and dependencies to persistent storage
     **/
    private void savePersistedData() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue("modpublish.changelog", changelogField.getText());
        List<DependencyInfo> dependencies = dependencyPanel.getDependencies();
        String dependenciesJson = JsonParser.toJson(dependencies);
        properties.setValue("modpublish.dependencies", dependenciesJson);
    }

    private @Nullable PublishResult doOKActionFirst() {
        PublishResult validationResult = validatePublishTargetSelection();
        if (validationResult != null) return validationResult;

        validationResult = validateModrinthCurseforgeSettings();
        if (validationResult != null) return validationResult;
        return validateVersionFields();
    }

    private @Nullable PublishResult validatePublishTargetSelection() {
        if (!curseforgeCheckBox.isSelected() && !modrinthCheckBox.isSelected()
                && !githubCheckBox.isSelected()) {
            return PublishResult.of("failed.1");
        }
        return null;
    }

    private PublishResult validateModrinthCurseforgeSettings() {
        boolean hasModrinthOrCurseforge = modrinthCheckBox.isSelected()
                || curseforgeCheckBox.isSelected();

        if (hasModrinthOrCurseforge && !clientCheckBox.isSelected() && !serverCheckBox.isSelected()) {
            return PublishResult.of("failed.2");
        }

        if (hasModrinthOrCurseforge && loaderCheckBoxes.stream().noneMatch(JBCheckBox::isSelected)) {
            return PublishResult.of("failed.3");
        }

        return null;
    }

    private @Nullable PublishResult validateVersionFields() {
        if (versionNameField.getText() == null || versionNameField.getText().trim().isEmpty()) {
            return PublishResult.of("failed.5");
        }
        if (versionNumberField.getText() == null || versionNumberField.getText().trim().isEmpty()) {
            return PublishResult.of("failed.6");
        }
        return null;
    }

    @Override
    protected void doOKAction() {
        PublishResult fr = doOKActionFirst();
        if (fr != null) {
            showFailedDialogRaw(get("message.failed", fr.result()),
                    get("title.failed"));
            return;
        }

        getOKAction().setEnabled(false);
        setOKButtonLoading();
        setText("button.publishing", TextType.OKButton);

        Async.runAsync(() -> {
            savePersistedData();
            PublishData publishData = collectPublishData();

            List<PublishResult> result = performPublish(publishData);
            boolean isOk = true;
            StringBuilder builder = new StringBuilder();
            for (PublishResult r : result) {
                if (r.ID().isBlank() && r.isFailure()) {
                    isOk = false;
                    builder.append(r.result());
                    break;
                }
                if (r.isFailure()) {
                    if (isOk) isOk = false;
                    builder.append("\n");
                    builder.append(r.ID());
                    builder.append(": ");
                    builder.append(r.result());
                }
            }

            boolean finalIsOk = isOk;
            SwingUtilities.invokeLater(() -> {
                setOKButtonDefault();
                getOKAction().setEnabled(true);
                setText("button.publish", TextType.OKButton);

                if (finalIsOk) {
                    super.doOKAction();
                    showSuccessDialog("message.success", "title.success");
                    close(0, true);
                } else {
                    showFailedDialogRaw(get("message.failed", builder.toString()),
                            get("title.failed"));
                }
            });
        });
    }

    private @NotNull PublishData collectPublishData() {
        List<LauncherInfo> selectedLoaders = new ArrayList<>();
        for (int i = 0; i < loaderCheckBoxes.size(); i++) {
            if (loaderCheckBoxes.get(i).isSelected())
                selectedLoaders.add(launchers.get(i));
        }

        List<MinecraftVersion> selectedMinecraftVersions = new ArrayList<>();
        for (int i = 0; i < minecraftVersionModel.getSize(); i++) {
            MinecraftVersionItem item = minecraftVersionModel.getElementAt(i);
            if (item.isSelected()) selectedMinecraftVersions.add(item.getVersion());
        }
        ReleaseChannel rT = this.releaseType.getItemAt(this.releaseType.getSelectedIndex());
        if (clientCheckBox.isSelected())
            supportedInfo.client.setEnabled(true);
        if (serverCheckBox.isSelected())
            supportedInfo.server.setEnabled(true);

        File[] files = new File[jarFile.length];
        for (int i = 0; i < jarFile.length; i++)
            files[i] = FileAPI.toFile(jarFile[i]);

        return new PublishData(
                versionNameField.getText(),
                versionNumberField.getText(),
                getPublishTargets(),
                rT,
                selectedLoaders,
                supportedInfo,
                selectedMinecraftVersions,
                changelogField.getText(),
                dependencyPanel.getDependencies(),
                files
        );
    }

    private @NotNull List<PublishResult> performPublish(@NotNull PublishData data) {
        List<PublishResult> results = new ArrayList<>();
        if (data.minecraftVersions().isEmpty()) {
            results.add(PublishResult.of("failed.4"));
            return results;
        }

        try {
            @Nullable CompletableFuture<@Nullable PublishResult> curseForgeTask =
                    createPublishTask(TargetType.CurseForge.api, curseforgeCheckBox, data);

            @Nullable CompletableFuture<@Nullable PublishResult> modrinthTask =
                    createPublishTask(TargetType.Modrinth.api, modrinthCheckBox, data);

            @Nullable CompletableFuture<@Nullable PublishResult> githubTask =
                    createPublishTask(TargetType.Github.api, githubCheckBox, data);

            joinResult(results, curseForgeTask, modrinthTask, githubTask);
        } catch (CompletionException e) {
            results.add(PublishResult.of("failed.7", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
        return results;
    }

    @Nullable
    final CompletableFuture<@Nullable PublishResult> createPublishTask(@NotNull API api, @NotNull JBCheckBox checkBox, @NotNull PublishData data) {
        return checkBox.isSelected() ? Async.runAsync(() -> {
            PublishResult result = api.createVersion(data, project);
            if (result.isFailure()) return result;
            return null;
        }) : null;
    }

    @SafeVarargs
    final void joinResult(@NotNull List<PublishResult> list, @Nullable CompletableFuture<@Nullable PublishResult>... futures) {
        if (futures == null) return;
        for (@Nullable CompletableFuture<@Nullable PublishResult> future : futures) {
            if (future == null || future.isCancelled()) continue;
            @Nullable PublishResult result = future.join();
            if (result != null) list.add(result);
        }
    }

    public Selector getPublishTargets() {
        return Selector.of(modrinthCheckBox, curseforgeCheckBox, githubCheckBox);
    }
}