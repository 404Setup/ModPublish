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
package one.pkg.modpublish.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.ModInfos
import one.pkg.modpublish.data.internal.PublishTarget
import one.pkg.modpublish.data.internal.Selector
import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.data.local.DependencyType
import one.pkg.modpublish.settings.properties.Properties.getProperties
import one.pkg.modpublish.ui.base.BaseDialogWrapper
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

class AddDependencyDialog(
    parent: PublishModDialog,
    private val selector: Selector,
    private val existingDependency: DependencyInfo? = null
) : BaseDialogWrapper(parent.project, true) {

    private var modrinthIdField: JBTextField? = null
    private var curseforgeIdField: JBTextField? = null
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

        var row = 0

        val p = getProperties(requireNotNull(project))
        val modrinthTokenFailed = p.modrinth.token.failed
        val curseforgeStudioTokenFailed = p.curseforge.studioToken.failed

        if (selector.modrinth) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
            panel.add(getJBLabel("component.name.depend-id.modrinth"), gbc)

            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
            modrinthIdField = JBTextField(30).apply {
                if (modrinthTokenFailed) {
                    isEnabled = false
                    toolTipText = get("tooltip.modrinth.disable")
                }
            }.also { panel.add(it, gbc) }
            row++
        }

        if (selector.curseForge) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
            panel.add(getJBLabel("component.name.depend-id.curseforge"), gbc)

            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
            curseforgeIdField = JBTextField(30).apply {
                if (curseforgeStudioTokenFailed) {
                    isEnabled = false
                    toolTipText = get("failed.11")
                }
            }.also { panel.add(it, gbc) }
            row++
        }

        val pid = existingDependency?.projectId ?: ""
        if (pid.contains(",")) {
            val parts = pid.split(",", limit = 2)
            modrinthIdField?.text = parts[0]
            curseforgeIdField?.text = parts[1]
        } else {
            if (selector.modrinth) modrinthIdField?.text = pid
            if (selector.curseForge) curseforgeIdField?.text = pid
        }

        // Dependency type
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0
        panel.add(getJBLabel("component.name.depend-status"), gbc)

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        dependencyTypeCombo = ComboBox(DependencyType.entries.toTypedArray()).also {
            it.selectedItem = existingDependency?.type ?: DependencyType.REQUIRED
            panel.add(it, gbc)
        }

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

        val mId = modrinthIdField?.text?.trim() ?: ""
        val cId = curseforgeIdField?.text?.trim() ?: ""

        if (mId.isEmpty() && cId.isEmpty()) {
            showFailedDialog("failed.9", "title.failed")
            return
        }

        val projectId = if (selector.modrinth && selector.curseForge) {
            if (mId == cId && mId.isNotEmpty()) mId else "$mId,$cId"
        } else {
            if (selector.modrinth) mId else cId
        }

        val selectedType = dependencyTypeCombo.selectedItem as DependencyType
        resultDependency = DependencyInfo(projectId, selectedType, existingDependency?.customTitle)

        if (existingDependency != null && existingDependency.projectId == projectId) {
            resultDependency.modrinthModInfo = existingDependency.modrinthModInfo
            resultDependency.curseforgeModInfo = existingDependency.curseforgeModInfo
            isDone = true
            super.doOKAction()
            return
        }

        validateDependency(resultDependency).apply {
            modrinth?.failed?.let {
                showFailedDialogRaw(it, get("title.failed"))
                return
            }
            curseForge?.failed?.let {
                showFailedDialogRaw(it, get("title.failed"))
                return
            }

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
                PublishTarget.Modrinth.api.getModInfo(parts[0], requireNotNull(project)).also {
                    if (it.failed != null) return ModInfos(it, null)
                }
            } else null

            val curseforgeInfo = if (selector.curseForge && parts[1].isNotBlank()) {
                if (getProperties(requireNotNull(project)).curseforge.studioToken.failed) {
                    return ModInfos(null, ModInfo.of(get("failed.11")))
                }
                PublishTarget.CurseForge.api.getModInfo(parts[1], requireNotNull(project)).also {
                    if (it.failed != null) return ModInfos(null, it)
                }
            } else null

            return ModInfos(modrinthInfo, curseforgeInfo)
        } else {
            val modrinthInfo = if (selector.modrinth) {
                PublishTarget.Modrinth.api.getModInfo(projectId, requireNotNull(project))
                    .also { if (it.failed != null) return ModInfos(it, null) }
            } else null

            val curseforgeInfo = if (selector.curseForge) {
                if (getProperties(requireNotNull(project)).curseforge.studioToken.failed) {
                    return ModInfos(null, ModInfo.of(get("failed.11")))
                }
                PublishTarget.CurseForge.api.getModInfo(projectId, requireNotNull(project)).also {
                    if (it.failed != null) return ModInfos(null, it)
                }
            } else null

            return ModInfos(modrinthInfo, curseforgeInfo)
        }
    }

    fun getDependency(): DependencyInfo = resultDependency
}