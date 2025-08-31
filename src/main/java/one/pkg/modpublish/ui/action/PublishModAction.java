package one.pkg.modpublish.ui.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;
import one.pkg.modpublish.data.internel.ModType;
import one.pkg.modpublish.util.resources.Lang;
import one.pkg.modpublish.ui.PublishModDialog;
import one.pkg.modpublish.util.io.VirtualFileAPI;
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

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

        boolean shouldShow = false;
        boolean shouldEnable = false;

        if (file != null && !file.isDirectory()) {
            boolean isJarFile = file.getName().endsWith(".jar");
            if (isJarFile) {
                try {
                    ModType modType = ModType.of(VirtualFileAPI.toFile(file));
                    shouldShow = true;
                    shouldEnable = modType != null;
                } catch (Exception e) {
                    shouldShow = true;
                }
            }
        }

        Presentation presentation = event.getPresentation();
        presentation.setVisible(shouldShow);
        presentation.setEnabled(shouldEnable);
    }

}
