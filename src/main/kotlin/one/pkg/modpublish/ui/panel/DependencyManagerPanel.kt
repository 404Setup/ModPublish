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
package one.pkg.modpublish.ui.panel

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.ui.AddDependencyDialog
import one.pkg.modpublish.ui.PublishModDialog
import one.pkg.modpublish.util.resources.Lang
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class DependencyManagerPanel(private val parentDialog: PublishModDialog) : JPanel(BorderLayout()) {
    private val dependencies: MutableList<DependencyInfo> = ArrayList()
    private val dependencyListPanel: JPanel

    init {
        val titleLabel = JBLabel(Lang.get("component.name.depend-manager")).apply {
            font = font.deriveFont(Font.BOLD, 14f)
        }

        val addButton = JButton(Lang.get("title.add-dependency")).apply {
            addActionListener {
                onAddDependency()
            }
        }

        val headerPanel = JPanel(BorderLayout()).apply {
            add(titleLabel, BorderLayout.WEST)
            add(addButton, BorderLayout.EAST)
        }

        dependencyListPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        val scrollPane = JBScrollPane(dependencyListPanel).apply {
            preferredSize = Dimension(600, 150)
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        }

        add(headerPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }

    private fun onAddDependency() {
        val selector = parentDialog.getPublishTargets()

        val dialog = AddDependencyDialog(parentDialog, selector)
        if (dialog.showAndGet() && dialog.isOK) addDependency(dialog.getDependency())
    }

    fun addDependency(dependency: DependencyInfo) {
        dependencies.add(dependency)
        refreshDependencyList()
    }

    fun removeDependency(dependency: DependencyInfo) {
        dependencies.remove(dependency)
        refreshDependencyList()
    }

    private fun refreshDependencyList() {
        dependencyListPanel.removeAll()

        for (dependency in dependencies) {
            val depPanel = createDependencyPanel(dependency)
            dependencyListPanel.add(depPanel)
        }

        dependencyListPanel.revalidate()
        dependencyListPanel.repaint()
    }

    private fun createDependencyPanel(dependency: DependencyInfo): JPanel {
        return JPanel(BorderLayout()).apply {
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10))

            val displayText = String.format(
                "%s (%s) - %s",
                if (dependency.customTitle != null && !dependency.customTitle!!.isBlank())
                    dependency.customTitle
                else "Unknown Dependency",
                dependency.projectId,
                dependency.type.displayName
            )

            add(JBLabel(displayText), BorderLayout.CENTER)
            add(JButton(Lang.get("button.delete")).apply {
                addActionListener {
                    removeDependency(dependency)
                }
            }, BorderLayout.EAST)
        }
    }

    fun getDependencies(): List<DependencyInfo> {
        return ArrayList(dependencies)
    }

    fun setDependencies(dependencies: List<DependencyInfo>?) {
        this.dependencies.clear()
        if (dependencies != null) this.dependencies.addAll(dependencies)
        refreshDependencyList()
    }
}