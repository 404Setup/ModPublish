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
import one.pkg.modpublish.PluginMain;
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
import one.pkg.modpublish.ui.panel.DependencyManagerPanel;
import one.pkg.modpublish.ui.renderer.CheckBoxListCellRenderer;
import one.pkg.modpublish.util.io.JsonParser;
import one.pkg.modpublish.util.io.VirtualFileAPI;
import one.pkg.modpublish.util.resources.Lang;
import one.pkg.modpublish.util.resources.LocalResources;
import one.pkg.modpublish.util.version.constraint.VersionConstraint;
import one.pkg.modpublish.util.version.constraint.VersionConstraintParser;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PublishModDialog extends BaseDialogWrapper {

    private final Project project;
    private final VirtualFile jarFile;
    private final List<ModType> modTypes;

    // UI Components
    private JBTextField versionNameField;
    private JBTextField versionNumberField;

    // Publish targets
    private JBCheckBox githubCheckBox;
    private JBCheckBox gitlabCheckBox;
    private JBCheckBox modrinthCheckBox;
    private JBCheckBox modrinthTestCheckBox;
    private JBCheckBox curseforgeCheckBox;

    // Support targets
    private JBCheckBox clientCheckBox;
    private JBCheckBox serverCheckBox;
    private JComboBox<ReleaseChannel> releaseType;

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
    private List<MinecraftVersion> minecraftVersions;
    private List<LauncherInfo> launchers;
    private SupportedInfo supportedInfo;

    public PublishModDialog(Project project, VirtualFile jarFile) {
        super(project);
        this.project = project;
        this.jarFile = jarFile;
        this.modTypes = ModType.getAll(jarFile);

        PluginMain.updateProject(project);

        setTitle("title.publish", jarFile.getName());
        setModal(true);

        loadConfigData();
        init();
        setText("button.publish", TextType.OKButton);
        setText("button.cancel", TextType.CancelButton);
    }

    private void loadConfigData() {
        try {
            minecraftVersions = LocalResources.getMinecraftVersions();
            launchers = LocalResources.getLauncherInfo();
            supportedInfo = LocalResources.getSupportedInfo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected JComponent createCenterPanel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        // Version name and number
        versionNameField = new JBTextField();
        versionNumberField = new JBTextField();

        formBuilder.addLabeledComponent(Lang.get("component.name.version-name"), versionNameField);
        formBuilder.addLabeledComponent(Lang.get("component.name.version-number"), versionNumberField);

        // Publish targets
        githubCheckBox = new JBCheckBox("GitHub");
        gitlabCheckBox = new JBCheckBox("GitLab");
        modrinthCheckBox = new JBCheckBox("Modrinth");
        modrinthTestCheckBox = new JBCheckBox("Modrinth (Test Server)");
        curseforgeCheckBox = new JBCheckBox("CurseForge");

        JPanel publishTargetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        publishTargetsPanel.add(githubCheckBox);
        publishTargetsPanel.add(gitlabCheckBox);
        publishTargetsPanel.add(modrinthCheckBox);
        publishTargetsPanel.add(modrinthTestCheckBox);
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
        for (LauncherInfo launcher : launchers) {
            JBCheckBox checkBox = new JBCheckBox(launcher.n);
            if (modTypes.contains(ModType.of(launcher.n.toLowerCase())))
                checkBox.setSelected(true);
            loaderCheckBoxes.add(checkBox);
            loadersPanel.add(checkBox);
        }
        formBuilder.addLabeledComponent(get("component.name.loaders"), loadersPanel);

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


        JPanel minecraftPanel = new JPanel(new BorderLayout());
        minecraftPanel.add(minecraftScrollPane, BorderLayout.CENTER);
        minecraftPanel.add(showSnapshotsCheckBox, BorderLayout.SOUTH);
        formBuilder.addLabeledComponent(get("component.name.mc-version"), minecraftPanel);

        // Changelog
        try {
            MarkdownFileType markdownFileType = MarkdownFileType.INSTANCE;
            changelogField = new EditorTextField("", project, markdownFileType);
        } catch (Exception e) {
            changelogField = new EditorTextField("", project, PlainTextFileType.INSTANCE);
        }

        changelogField.setPreferredSize(new Dimension(500, 150));
        changelogField.setMinimumSize(new Dimension(500, 100));
        changelogField.setOneLineMode(false);

        formBuilder.addLabeledComponent(Lang.get("component.name.changelog"), changelogField);

        // Dependency manager
        dependencyPanel = new DependencyManagerPanel(this);
        formBuilder.addLabeledComponent(Lang.get("component.name.dependencies"), dependencyPanel);

        autoFillFields();

        return toScrollPanel(formBuilder, 800, 650);
    }

    private void updateMinecraftVersions() {
        minecraftVersionModel.clear();
        boolean includeSnapshots = showSnapshotsCheckBox.isSelected();

        for (MinecraftVersion version : minecraftVersions) {
            if ("release".equals(version.t) || (includeSnapshots && "snapshot".equals(version.t))) {
                minecraftVersionModel.addElement(new MinecraftVersionItem(version, false));
            }
        }
    }


    private String extractVersionName(VirtualFile file) {
        return file.getNameWithoutExtension();
    }

    private void autoFillFields() {
        autoFillMinecraftVersions();

        loadModInfo();
        loadPersistedData();
    }

    private void loadModInfo() {
        String version = null;
        String versionName = extractVersionName(jarFile);
        String versionNameFormat = PID.CommonVersionFormat.get(project);

        ModType t = ModType.of(jarFile);
        if (t != null) {
            LocalModInfo lmInfo = t.getMod(jarFile);
            if (lmInfo != null) {
                version = lmInfo.version();
                VersionConstraint parser = VersionConstraintParser.parse(lmInfo.versionRange());
                String lowVersion = parser.getLowVersion();
                String highVersion = parser.getMaxVersion();

                if (!versionNameFormat.isEmpty()) {
                    versionName =
                            versionNameFormat.replace("{version}", version)
                                    .replace("{name}", lmInfo.name())
                                    .replace("{loader}", t.getName());
                    if (!lowVersion.isEmpty()) versionName = versionName.replace("{low-version}", lowVersion);
                    if (!highVersion.isEmpty()) versionName = versionName.replace("{high-version}", highVersion);
                }
            }
        }

        if (version == null) version = ModVersion.extractVersionNumber(jarFile);

        versionNameField.setText(versionName);
        versionNumberField.setText(version);
    }

    private void autoFillMinecraftVersions() {
        if (minecraftVersionModel.getSize() > 0) {
            minecraftVersionModel.getElementAt(0).setSelected(true);
            minecraftVersionList.repaint();
        }
    }

    private void setFailedSelect(JBCheckBox jbCheckBox) {
        jbCheckBox.setEnabled(false);
        setErrorStyle(jbCheckBox);
        setToolTipText("tooltip.decrypt.failed", jbCheckBox);
    }

    private void loadPersistedData() {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);

        Property p2 = Properties.getProperties(properties);

        if (!p2.modrinth().isModEnabled()) {
            modrinthCheckBox.setEnabled(false);
            setToolTipText("tooltip.modrinth.disable", modrinthCheckBox);
        } else if (p2.modrinth().token().failed())
            setFailedSelect(modrinthCheckBox);

        if (!p2.modrinth().isTestEnabled()) {
            modrinthTestCheckBox.setEnabled(false);
            setToolTipText("tooltip.modrinth.disable", modrinthTestCheckBox);
        } else if (p2.modrinth().testToken().failed())
            setFailedSelect(modrinthTestCheckBox);

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

        if (!p2.gitlab().isEnabled()) {
            gitlabCheckBox.setEnabled(false);
            setToolTipText("tooltip.git.disable", gitlabCheckBox, "Gitlab");
        } else if (p2.gitlab().token().failed())
            setFailedSelect(gitlabCheckBox);

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
        if (!curseforgeCheckBox.isSelected() && !modrinthCheckBox.isSelected()
                && !modrinthTestCheckBox.isSelected() && !githubCheckBox.isSelected()
                && !gitlabCheckBox.isSelected()) {
            return PublishResult.of("failed.1");
        }
        if (!clientCheckBox.isSelected() && !serverCheckBox.isSelected()) {
            return PublishResult.of("failed.2");
        }
        if (loaderCheckBoxes.stream().noneMatch(JBCheckBox::isSelected)) {
            return PublishResult.of("failed.3");
        }
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
            showMessageDialogRaw(get("message.failed", fr.result()),
                    get("title.failed"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        savePersistedData();

        PublishData publishData = collectPublishData();

        getOKAction().setEnabled(false);
        setText("button.publishing", TextType.OKButton);

        SwingUtilities.invokeLater(() -> {
            PublishResult result = performPublish(publishData);

            if (result.result() == null || result.result().trim().isEmpty()) {
                super.doOKAction();
                showMessageDialog("message.success", "title.success", JOptionPane.INFORMATION_MESSAGE);
                close(0, true);
            } else {
                getOKAction().setEnabled(true);
                setText("button.publish", TextType.OKButton);
                showMessageDialogRaw(get("message.failed", result.result()),
                        get("title.failed"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private PublishData collectPublishData() {
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

        return new PublishData(
                versionNameField.getText(),
                versionNumberField.getText(),
                PublishData.Enabled.getInstance(getPublishTargets()),
                rT,
                selectedLoaders,
                supportedInfo,
                selectedMinecraftVersions,
                changelogField.getText(),
                dependencyPanel.getDependencies(),
                VirtualFileAPI.toFile(jarFile)
        );
    }

    private PublishResult performPublish(PublishData data) {
        if (data.minecraftVersions() == null || data.minecraftVersions().isEmpty()) {
            return PublishResult.of("failed.4");
        }

        //try {
        var modrinthApi = TargetType.Modrinth.api;
        var curseforgeApi = TargetType.CurseForge.api;
        var githubApi = TargetType.Github.api;
        if (curseforgeCheckBox.isSelected()) {
            PublishResult cfResult = curseforgeApi.createVersion(data, project);
            if (cfResult.isFailure()) return cfResult;
        }

        if (modrinthCheckBox.isSelected()) {
            if (modrinthApi.getABServer()) modrinthApi.updateABServer();
            PublishResult mrResult = modrinthApi.createVersion(data, project);
            if (mrResult.isFailure()) return mrResult;
        }

        if (modrinthTestCheckBox.isSelected()) {
            if (!modrinthApi.getABServer()) modrinthApi.updateABServer();
            PublishResult mrResult = modrinthApi.createVersion(data, project);
            if (mrResult.isFailure()) return mrResult;
        }

        if (githubCheckBox.isSelected()) {
            PublishResult ghResult = githubApi.createVersion(data, project);
            if (ghResult.isFailure()) return ghResult;
        }

        return PublishResult.empty();
        /*} catch (InterruptedException e) {
            return PublishResult.of("failed.7", e.getMessage() != null ? e.getMessage() : "Unknown error");
        }*/
    }

    public boolean[] getPublishTargets() {
        return new boolean[]{
                githubCheckBox.isSelected(),
                gitlabCheckBox.isSelected(),
                modrinthCheckBox.isSelected(),
                modrinthTestCheckBox.isSelected(),
                curseforgeCheckBox.isSelected()
        };
    }

    public Project getProject() {
        return project;
    }
}