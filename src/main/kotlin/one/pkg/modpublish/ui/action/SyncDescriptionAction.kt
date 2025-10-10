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

package one.pkg.modpublish.ui.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import one.pkg.modpublish.ui.SyncDescriptionDialog
import one.pkg.modpublish.ui.icon.Icons
import one.pkg.modpublish.util.resources.Lang
import javax.swing.JOptionPane

class SyncDescriptionAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val file: VirtualFile? = event.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file != null && file.isFile && file.name.lowercase() == "readme.md") {
            SyncDescriptionDialog(file, event.project!!).show()
        } else {
            JOptionPane.showMessageDialog(
                null,
                Lang.get("message.invalid-file"), Lang.get("title.failed"), JOptionPane.WARNING_MESSAGE,
                Icons.Static.Warning
            )
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val presentation = event.presentation

        if (file != null && file.isFile && file.name.lowercase() == "readme.md") {
            presentation.isVisible = true
            presentation.isEnabled = true
        } else {
            presentation.isVisible = false
            presentation.isEnabled = false
        }
    }
}