package one.pkg.modpublish.settings;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import one.pkg.modpublish.protect.Protect;
import one.pkg.modpublish.protect.HardwareFingerprint;
import one.pkg.modpublish.resources.Lang;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ModPublishSettingsComponent {
    private final JPanel panel;
    private final JBTextField modrinthTokenText = new JBTextField();
    private final JBTextField modrinthTestTokenText = new JBTextField();
    private final JBTextField curseforgeTokenText = new JBTextField();
    private final JBTextField curseforgeStudioTokenText = new JBTextField();
    private final JBTextField githubTokenText = new JBTextField();
    private final JBTextField gitlabTokenText = new JBTextField();

    public ModPublishSettingsComponent() {
        JBLabel mL = new JBLabel("Modrinth token:");
        mL.setIcon(IconLoader.getIcon("/icons/modrinth.svg", getClass()));
        JBLabel mL2 = new JBLabel("Modrinth test token:");
        mL2.setIcon(IconLoader.getIcon("/icons/modrinth.svg", getClass()));
        JBLabel cL = new JBLabel("Curseforge token:");
        cL.setIcon(IconLoader.getIcon("/icons/curseforge.svg", getClass()));
        JBLabel cL2 = new JBLabel("Curseforge Studio token:");
        cL2.setIcon(IconLoader.getIcon("/icons/curseforge.svg", getClass()));
        JBLabel gL = new JBLabel("Github token:");
        gL.setIcon(IconLoader.getIcon("/icons/github.svg", getClass()));
        JBLabel gL2 = new JBLabel("Gitlab token:");
        gL2.setIcon(IconLoader.getIcon("/icons/gitlab.svg", getClass()));
        gitlabTokenText.setEnabled(false);
        gitlabTokenText.setToolTipText(Lang.get("tooltip.gitlab.disable"));

        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(mL, modrinthTokenText, 1, false)
                .addLabeledComponent(mL2, modrinthTestTokenText, 1, false)
                .addLabeledComponent(cL, curseforgeTokenText, 1, false)
                .addLabeledComponent(cL2, curseforgeStudioTokenText, 1, false)
                .addLabeledComponent(gL, githubTokenText, 1, false)
                .addLabeledComponent(gL2, gitlabTokenText, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return panel;
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
