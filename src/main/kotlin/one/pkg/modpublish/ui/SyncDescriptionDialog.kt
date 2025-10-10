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

package one.pkg.modpublish.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import one.pkg.modpublish.data.internal.Info
import one.pkg.modpublish.data.internal.PublishTarget
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.ui.base.BaseDialogWrapper
import one.pkg.modpublish.util.io.FileAPI.toFile
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class SyncDescriptionDialog(
    val file: VirtualFile,
    override val project: Project
) : BaseDialogWrapper(project, false) {

    private lateinit var modrinthID: String
    private lateinit var modrinthToken: Info
    private lateinit var publishTargetCombo: ComboBox<PublishTarget>

    init {
        title = "action.modpublish.action.patch-description.text"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply { insets = JBUI.insets(5); anchor = GridBagConstraints.WEST }

        // Dependency type
        gbc.gridx = 0;
        panel.add(getJBLabel("component.name.targets"), gbc)

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        publishTargetCombo = ComboBox(arrayOf(PublishTarget.Modrinth)).apply {
            renderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean
                ): Component {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    if (value is PublishTarget) text = value.api.id
                    return this
                }
            }

            modrinthID = PID.ModrinthModID.get(project)
            modrinthToken = PID.ModrinthToken.getProtect(project)
            if (modrinthID.isEmpty() || modrinthToken.failed) removeItem(PublishTarget.Modrinth)
            if (itemCount < 1) addItem(PublishTarget.Empty)
        }.also {
            panel.add(it, gbc)
        }

        return panel
    }

    override fun doOKAction() {
        val selectedType = publishTargetCombo.selectedItem as PublishTarget
        if (selectedType == PublishTarget.Empty) {
            showFailedDialogRaw(
                selectedType.api.id,
                get("title.failed")
            )
            return
        }

        val result = selectedType.api.patchDescription(modrinthID, file.toFile().readText(Charsets.UTF_8), project)
        if (result.isFailure) {
            showFailedDialogRaw(
                result.result!!,
                get("title.failed")
            )
        } else {
            showSuccessDialog("message.success", "title.success")
            super.doOKAction()
        }
    }
}