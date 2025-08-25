package one.pkg.modpublish;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
public final class PluginMain {
    private static Project project;

    public PluginMain(Project project) {
        PluginMain.project = project;
    }


    public static Project getProject() {
        return project;
    }

    public static void updateProject(Project project) {
        if (PluginMain.project == null || !PluginMain.project.equals(project))
            PluginMain.project = project;
    }
}