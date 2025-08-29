package one.pkg.modpublish.settings;

import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import one.pkg.modpublish.protect.HardwareFingerprint;
import one.pkg.modpublish.protect.Protect;
import one.pkg.modpublish.resources.Lang;
import one.pkg.modpublish.ui.base.BaseDialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


public class ModPublishSettingsComponent extends BaseDialogWrapper {
    private final JPanel panel;
    private JBTextField modrinthTokenText;
    private JBTextField modrinthTestTokenText;
    private JBTextField curseforgeTokenText;
    private JBTextField curseforgeStudioTokenText;
    private JBTextField githubTokenText;
    private JBTextField gitlabTokenText;

    public ModPublishSettingsComponent() {
        super(null);
        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        addPlatformSection(formBuilder, "Modrinth", "/icons/modrinth.svg",
                new FieldConfig("Token", () -> modrinthTokenText = createTextField()),
                new FieldConfig("(Test) Token", () -> modrinthTestTokenText = createTextField()));

        addPlatformSection(formBuilder, "CurseForge", "/icons/curseforge.svg",
                new FieldConfig("Token", () -> curseforgeTokenText = createTextField()),
                new FieldConfig("Studio Token", () -> curseforgeStudioTokenText = createTextField()));

        addPlatformSection(formBuilder, "GitHub", "/icons/github.svg",
                new FieldConfig("Token", () -> githubTokenText = createTextField()));
        addPlatformSection(formBuilder, "Gitlab", "/icons/gitlab.svg",
                new FieldConfig("Token", () -> gitlabTokenText = createTextField()));

        gitlabTokenText.setEnabled(false);
        gitlabTokenText.setToolTipText(Lang.get("tooltip.gitlab.disable"));

        panel = formBuilder.addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return panel;
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
