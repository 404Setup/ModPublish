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
import one.pkg.modpublish.data.internal.ModType.Companion.toModType
import one.pkg.modpublish.ui.PublishModDialog
import one.pkg.modpublish.ui.icon.Icons
import one.pkg.modpublish.util.io.FileAPI.toFile
import one.pkg.modpublish.util.resources.Lang
import javax.swing.JOptionPane

class PublishModAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        var file: Array<VirtualFile>? = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        if (file == null || file.isEmpty()) {
            val f = event.getData(CommonDataKeys.VIRTUAL_FILE)
            file = if (f != null) arrayOf(f) else arrayOf()
        }
        if (file.isNotEmpty()) {
            val list: MutableList<VirtualFile> = ArrayList()
            for (virtualFile in file) {
                if (!virtualFile.isDirectory && virtualFile.name
                        .endsWith(".jar") && virtualFile.toFile().toModType() != null
                ) {
                    list.add(virtualFile)
                }
            }
            if (!list.isEmpty()) {
                PublishModDialog(
                    event.project,
                    list.toTypedArray()
                ).show()
                return
            }
        }
        JOptionPane.showMessageDialog(
            null,
            Lang.get("message.invalid-file"), Lang.get("title.failed"), JOptionPane.WARNING_MESSAGE,
            Icons.Static.Warning
        )
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)

        var shouldShow = false
        var shouldEnable = false

        if (file != null && !file.isDirectory) {
            val isJarFile = file.name.endsWith(".jar")
            if (isJarFile) {
                try {
                    val modType = file.toFile().toModType()
                    shouldShow = true
                    shouldEnable = modType != null
                } catch (_: Exception) {
                    shouldShow = true
                }
            }
        }

        val presentation = event.presentation
        presentation.isVisible = shouldShow
        presentation.isEnabled = shouldEnable
    }
}
