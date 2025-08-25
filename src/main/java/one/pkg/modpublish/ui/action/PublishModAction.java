package one.pkg.modpublish.ui.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import one.pkg.modpublish.data.modinfo.ModType;
import one.pkg.modpublish.resources.Lang;
import one.pkg.modpublish.ui.PublishModDialog;
import one.pkg.modpublish.util.VirtualFileAPI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PublishModAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file != null && file.getName().endsWith(".jar") && ModType.of(VirtualFileAPI.toFile(file)) != null) {
            // Open the publish dialog
            new PublishModDialog(event.getProject(), file).show();
        } else {
            JOptionPane.showMessageDialog(null,
                    Lang.get("message.invalid-file"), Lang.get("title.failed"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /*@Override
    public void update(@NotNull AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean isJarFile = file != null && file.getName().endsWith(".jar");
        event.getPresentation().setVisible(isJarFile);
        event.getPresentation().setEnabled(isJarFile);
    }*/
}
