package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import one.pkg.modpublish.data.internel.Info;

import javax.swing.*;

@SuppressWarnings("unused")
public enum PID {
    ModrinthModID("modpublish.modrinth.modid", false), ModrinthTestModID("modpublish.modrinth.testModId", false),
    ModrinthToken("modpublish.modrinth.token", true), ModrinthTestToken("modpublish.modrinth.testToken", true),
    CurseForgeToken("modpublish.curseforge.token", true), CurseForgeStudioToken("modpublish.curseforge.studioToken", true),
    CurseForgeModID("modpublish.curseforge.modid", false),
    GithubToken("modpublish.github.token", true), GithubRepo("modpublish.github.repo", false), GithubBranch("modpublish.github.branch", false),
    GitlabToken("modpublish.gitlab.token", true), GitlabRepo("modpublish.gitlab.repo", false), GitlabBranch("modpublish.gitlab.branch", false);

    public final String id;
    public final boolean protect;

    PID(String id, boolean protect) {
        this.id = id;
        this.protect = protect;
    }

    public String get(Project project) {
        return get(Properties.getPropertiesComponent(project));
    }

    public String get(PropertiesComponent properties) {
        return properties.getValue(id, "");
    }

    public Info getProtect(Project project) {
        return getProtect(PropertiesComponent.getInstance(project));
    }

    public Info getProtect(PropertiesComponent properties) {
        if (!protect) return Info.of(get(properties));
        return Properties.getProtectValue(properties, this);
    }

    public void set(Project project, String data) {
        set(PropertiesComponent.getInstance(project), data);
    }

    public void set(PropertiesComponent properties, String data) {
        if (protect) Properties.setProtectValue(properties, id, data);
        else properties.setValue(id, data);
    }

    public void set(Project project, JTextField component) {
        set(project, component.getText());
    }

    public void set(PropertiesComponent properties, JTextField component) {
        set(properties, component.getText());
    }
}
