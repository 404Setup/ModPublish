/*
 * Copyright (C) 2025 - 2026 404Setup (https://github.com/404Setup)
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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.vfs.VirtualFile
import one.pkg.modpublish.data.internal.PublishType.Companion.toModType
import one.pkg.modpublish.ui.PublishModDialog
import one.pkg.modpublish.ui.icon.Icons
import one.pkg.modpublish.util.io.FileAPI.toFile
import one.pkg.modpublish.util.resources.Lang
import javax.swing.JOptionPane

class PublishModAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        var file: Array<VirtualFile>? = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        if (file.isNullOrEmpty()) {
            val f = event.getData(CommonDataKeys.VIRTUAL_FILE)
            file = if (f != null) arrayOf(f) else arrayOf()
        }

        val candidates = mutableListOf<VirtualFile>()
        for (virtualFile in file) {
            if (!virtualFile.isDirectory &&
                (virtualFile.name.endsWith(".jar") || virtualFile.name.endsWith(".litemod"))
            ) {
                candidates.add(virtualFile)
            }
        }

        if (candidates.isNotEmpty()) {
            val project = event.project
            object : Task.Backgroundable(project, "Scanning Mod Files", true) {
                override fun run(indicator: ProgressIndicator) {
                    val validFiles = candidates.filter {
                        indicator.checkCanceled()
                        it.toFile().toModType() != null
                    }

                    ApplicationManager.getApplication().invokeLater {
                        if (validFiles.isNotEmpty()) {
                            PublishModDialog(
                                project,
                                validFiles.toTypedArray()
                            ).show()
                        } else {
                            showInvalidFileMessage()
                        }
                    }
                }
            }.queue()
            return
        }

        showInvalidFileMessage()
    }

    private fun showInvalidFileMessage() {
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
            val isJarFile = file.name.endsWith(".jar") || file.name.endsWith(".litemod")
            if (isJarFile) {
                runCatching {
                    val modType = file.toFile().toModType()
                    shouldShow = true
                    shouldEnable = modType != null
                }.onFailure { shouldShow = true }
            }
        }

        val presentation = event.presentation
        presentation.isVisible = shouldShow
        presentation.isEnabled = shouldEnable
    }
}
