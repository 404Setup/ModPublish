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
package one.pkg.modpublish.ui.panel

import com.intellij.ui.components.JBLabel
import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.JBScrollPane
import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.ui.AddDependencyDialog
import one.pkg.modpublish.ui.PublishModDialog
import one.pkg.modpublish.util.resources.Lang
import one.pkg.modpublish.util.resources.Lang.translate
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.util.IdentityHashMap
import javax.swing.*

class DependencyManagerPanel(private val parentDialog: PublishModDialog) : JPanel(BorderLayout()) {
    private val dependencies: MutableList<DependencyInfo> = ArrayList()
    private val dependencyPanels = IdentityHashMap<DependencyInfo, JPanel>()
    private val dependencyListPanel: JPanel

    init {
        val titleLabel = JBLabel("component.name.depend-manager".translate()).apply {
            font = font.deriveFont(Font.BOLD, 14f)
        }

        val addButton = JButton("title.add-dependency".translate()).apply {
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

    private fun onEditDependency(dependency: DependencyInfo) {
        val selector = parentDialog.getPublishTargets()
        val dialog = AddDependencyDialog(parentDialog, selector, dependency)
        if (dialog.showAndGet() && dialog.isOK) {
            val index = dependencies.indexOf(dependency)
            if (index != -1) {
                val newDependency = dialog.getDependency()
                dependencies[index] = newDependency

                val oldPanel = dependencyPanels.remove(dependency)
                if (oldPanel != null) {
                    val compIndex = dependencyListPanel.components.indexOf(oldPanel)
                    dependencyListPanel.remove(oldPanel)

                    val newPanel = createDependencyPanel(newDependency)
                    dependencyPanels[newDependency] = newPanel
                    if (compIndex >= 0 && compIndex <= dependencyListPanel.componentCount) {
                        dependencyListPanel.add(newPanel, compIndex)
                    } else {
                        dependencyListPanel.add(newPanel)
                    }

                    dependencyListPanel.revalidate()
                    dependencyListPanel.repaint()
                }
            }
        }
    }

    fun addDependency(dependency: DependencyInfo) {
        dependencies.add(dependency)
        val depPanel = createDependencyPanel(dependency)
        dependencyPanels[dependency] = depPanel
        dependencyListPanel.add(depPanel)
        dependencyListPanel.revalidate()
        dependencyListPanel.repaint()
    }

    fun removeDependency(dependency: DependencyInfo) {
        dependencies.remove(dependency)
        val panel = dependencyPanels.remove(dependency)
        if (panel != null) {
            dependencyListPanel.remove(panel)
        }
        dependencyListPanel.revalidate()
        dependencyListPanel.repaint()
    }

    private fun createDependencyPanel(dependency: DependencyInfo): JPanel {
        return JPanel(BorderLayout()).apply {
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10))

            val displayText = String.format(
                "%s (%s) - %s",
                if (!dependency.customTitle.isNullOrBlank()) dependency.customTitle else "Unknown Dependency",
                dependency.projectId,
                dependency.type.translationKey.translate()
            )

            add(JBLabel(displayText), BorderLayout.CENTER)

            val actionPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0)).apply {
                add(JButton("button.open".translate()).apply {
                    addActionListener {
                        dependency.modrinthModInfo?.slug?.let { BrowserUtil.browse("https://modrinth.com/mod/$it") }
                        dependency.curseforgeModInfo?.slug?.let { BrowserUtil.browse("https://www.curseforge.com/minecraft/mc-mods/$it") }
                    }
                })
                add(JButton("button.edit".translate()).apply {
                    addActionListener {
                        onEditDependency(dependency)
                    }
                })
                add(JButton("button.delete".translate()).apply {
                    addActionListener {
                        removeDependency(dependency)
                    }
                })
            }

            add(actionPanel, BorderLayout.EAST)
        }
    }

    fun getDependencies(): List<DependencyInfo> = ArrayList(dependencies)

    fun setDependencies(dependencies: List<DependencyInfo>?) {
        val incoming = dependencies ?: emptyList()
        val incomingSet = incoming.toSet()
        // Convert incoming to a Set to reduce membership check complexity from O(N*M) to O(N)
        val toRemove = this.dependencies.filter { it !in incomingSet }
        for (dep in toRemove) {
            this.dependencies.remove(dep)
            dependencyPanels.remove(dep)?.let { dependencyListPanel.remove(it) }
        }
        for (i in incoming.indices) {
            val dep = incoming[i]
            if (dep !in this.dependencies) {
                this.dependencies.add(i, dep)
                val panel = createDependencyPanel(dep)
                dependencyPanels[dep] = panel
                dependencyListPanel.add(panel, i)
            } else {
                val currentIndex = this.dependencies.indexOf(dep)
                if (currentIndex != i) {
                    this.dependencies.removeAt(currentIndex)
                    this.dependencies.add(i, dep)
                    dependencyPanels[dep]?.let {
                        dependencyListPanel.remove(it)
                        dependencyListPanel.add(it, i)
                    }
                }
            }
        }
        dependencyListPanel.revalidate()
        dependencyListPanel.repaint()
    }
}