package one.pkg.modpublish.ui.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import one.pkg.modpublish.ui.ConfigProjectDialog;
import org.jetbrains.annotations.NotNull;

public class ConfigProjectAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        new ConfigProjectDialog(event.getProject()).show();
    }
}
