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

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.ModInfos
import one.pkg.modpublish.data.internal.Selector
import one.pkg.modpublish.data.internal.TargetType
import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.data.local.DependencyType
import one.pkg.modpublish.ui.base.BaseDialogWrapper
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

class AddDependencyDialog(
    parent: PublishModDialog,
    private val selector: Selector
) : BaseDialogWrapper(parent.project, true) {

    private lateinit var projectIdField: JBTextField
    private lateinit var dependencyTypeCombo: ComboBox<DependencyType>
    var isDone: Boolean = false
        private set
    private lateinit var resultDependency: DependencyInfo

    init {
        title = "title.add-dependency"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply { insets = JBUI.insets(5); anchor = GridBagConstraints.WEST }

        // Project ID
        gbc.gridx = 0; gbc.gridy = 0
        panel.add(getJBLabel("component.name.depend-id"), gbc)

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        projectIdField = JBTextField(30).also { panel.add(it, gbc) }

        // Help text
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        panel.add(createLabel(get("tips.1")), gbc)

        // Dependency type
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1
        panel.add(getJBLabel("component.name.depend-status"), gbc)

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        dependencyTypeCombo = ComboBox(DependencyType.entries.toTypedArray()).also { panel.add(it, gbc) }

        return panel
    }

    override fun doOKAction() {
        if (!selector.modrinth && !selector.curseForge) {
            showFailedDialog(
                if (selector.github || selector.gitlab) "message.dont-support-add-depends" else "failed.8",
                "title.failed"
            )
            return
        }

        val projectId = projectIdField.text.trim()
        if (projectId.isEmpty()) {
            showFailedDialog("failed.9", "title.failed")
            return
        }

        val selectedType = dependencyTypeCombo.selectedItem as DependencyType
        resultDependency = DependencyInfo(projectId, selectedType, null)

        validateDependency(resultDependency).apply {
            modrinth?.failed?.let { showFailedDialogRaw(it, get("title.failed")) }

            modrinth?.let { resultDependency.apply { customTitle = it.name; modrinthModInfo = it } }
            curseForge?.let { resultDependency.apply { customTitle = it.name; curseforgeModInfo = it } }
        }

        isDone = true
        super.doOKAction()
    }

    private fun validateDependency(dependency: DependencyInfo): ModInfos {
        val projectId = dependency.projectId
        if (projectId.isNullOrBlank()) return ModInfos(ModInfo.of("Project ID cannot be empty"), null)

        if (projectId.contains(",")) {
            val parts = projectId.split(",", limit = 2)
            if (parts.size != 2) return ModInfos(ModInfo.of("Invalid project ID format"), null)

            val modrinthInfo = if (selector.modrinth && parts[0].isNotBlank()) {
                TargetType.Modrinth.api.getModInfo(parts[0], project!!).also {
                    if (it.failed != null) return ModInfos(it, null)
                }
            } else null

            val curseforgeInfo = if (selector.curseForge && parts[1].isNotBlank()) {
                TargetType.CurseForge.api.getModInfo(parts[1], project!!).also {
                    if (it.failed != null) return ModInfos(null, it)
                }
            } else null

            return ModInfos(modrinthInfo, curseforgeInfo)
        } else {
            val modrinthInfo = if (selector.modrinth) {
                TargetType.Modrinth.api.apply { if (getAB()) updateAB() }
                    .getModInfo(projectId, project!!).also {
                        if (it.failed != null) return ModInfos(it, null)
                    }
            } else null

            val curseforgeInfo = if (selector.curseForge) {
                TargetType.CurseForge.api.getModInfo(projectId, project!!).also {
                    if (it.failed != null) return ModInfos(null, it)
                }
            } else null

            return ModInfos(modrinthInfo, curseforgeInfo)
        }
    }

    fun getDependency(): DependencyInfo = resultDependency
}