package one.pkg.modpublish.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import one.pkg.modpublish.settings.properties.PID;
import one.pkg.modpublish.settings.properties.Properties;
import one.pkg.modpublish.settings.properties.Property;
import one.pkg.modpublish.ui.base.BaseDialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

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

    private JBTextField gitlabTokenField;
    private JBTextField gitlabRepoField;
    private JBTextField gitlabBranchField;

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

        addPlatformSection(formBuilder, "Common", null,
                new FieldConfig(get("component.name.common.version-format"), () -> commonVersionFormatField = createTextField()));

        addPlatformSection(formBuilder, "Modrinth", "/icons/modrinth.svg",
                new FieldConfig(token, () -> modrinthTokenField = createTextField()),
                new FieldConfig("(Test) " + token, () -> modrinthTestTokenField = createTextField()),
                new FieldConfig(modIdLabel, () -> modrinthModIDField = createTextField()),
                new FieldConfig("(Test) " + modIdLabel, () -> modrinthTestModIDField = createTextField()));

        addPlatformSection(formBuilder, "CurseForge", "/icons/curseforge.svg",
                new FieldConfig(token, () -> curseforgeTokenField = createTextField()),
                new FieldConfig(studioToken, () -> curseforgeStudioTokenField = createTextField()),
                new FieldConfig(modIdLabel, () -> curseforgeModIDField = createTextField()));

        addPlatformSection(formBuilder, "GitHub", "/icons/github.svg",
                new FieldConfig(token, () -> githubTokenField = createTextField()),
                new FieldConfig(repoLabel, () -> {
                    githubRepoField = createTextField();
                    githubRepoField.setToolTipText(get("dialog.modpublish.config-project.repo.tooltips"));
                    return githubRepoField;
                }),
                new FieldConfig(branchLabel, () -> {
                    githubBranchField = createTextField();
                    githubBranchField.setToolTipText(get("dialog.modpublish.config-project.branch.tooltips"));
                    return githubBranchField;
                }));

        addPlatformSection(formBuilder, "Gitlab", "/icons/gitlab.svg",
                new FieldConfig(token, () -> {
                    gitlabTokenField = createTextField();
                    setDisabledWithTooltip(gitlabTokenField);
                    return gitlabTokenField;
                }),
                new FieldConfig(repoLabel, () -> {
                    gitlabRepoField = createTextField();
                    setDisabledWithTooltip(gitlabRepoField);
                    return gitlabRepoField;
                }),
                new FieldConfig(branchLabel, () -> {
                    gitlabBranchField = createTextField();
                    setDisabledWithTooltip(gitlabBranchField);
                    return gitlabBranchField;
                }));

        autoFillFields();

        JPanel panel = formBuilder.getPanel();
        panel.setBorder(JBUI.Borders.empty(20, 20, 15, 20));

        JBScrollPane scrollPane = new JBScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(600, 360));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
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

        gitlabTokenField.setText(p1.gitlab().token().globalData() ? "" : p1.gitlab().token().data());
        if (p1.gitlab().token().globalData() && gitlabTokenField.isEnabled())
            setToolTipText("dialog.modpublish.config-project.global", gitlabTokenField);
        gitlabRepoField.setText(p1.gitlab().repo());
        gitlabBranchField.setText(p1.gitlab().branch());
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

        PID.GitlabToken.set(properties, gitlabTokenField);
        PID.GitlabRepo.set(properties, gitlabRepoField);
        PID.GitlabBranch.set(properties, gitlabBranchField);
    }

    public void setDisabledWithTooltip(@NotNull JBTextField field) {
        field.setEnabled(false);
        field.setToolTipText(get("tooltip.gitlab.disable"));
    }
}
