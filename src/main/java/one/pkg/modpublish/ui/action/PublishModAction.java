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

package one.pkg.modpublish.ui.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.VirtualFile;
import one.pkg.modpublish.data.internel.ModType;
import one.pkg.modpublish.ui.PublishModDialog;
import one.pkg.modpublish.util.io.FileAPI;
import one.pkg.modpublish.util.resources.Lang;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PublishModAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file != null && file.getName().endsWith(".jar") && ModType.of(FileAPI.toFile(file)) != null) {
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
                    ModType modType = ModType.of(FileAPI.toFile(file));
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
