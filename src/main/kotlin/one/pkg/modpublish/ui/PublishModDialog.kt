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

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import one.pkg.modpublish.api.API
import one.pkg.modpublish.data.internal.*
import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.data.local.MinecraftVersion
import one.pkg.modpublish.data.local.SupportedInfo
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.settings.properties.Properties
import one.pkg.modpublish.ui.base.BaseDialogWrapper
import one.pkg.modpublish.ui.base.FieldConfig
import one.pkg.modpublish.ui.icon.Icons
import one.pkg.modpublish.ui.panel.DependencyManagerPanel
import one.pkg.modpublish.ui.renderer.CheckBoxListCellRenderer
import one.pkg.modpublish.util.io.Async
import one.pkg.modpublish.util.io.Async.async
import one.pkg.modpublish.util.io.FileAPI
import one.pkg.modpublish.util.io.FileAPI.toFile
import one.pkg.modpublish.util.io.JsonParser.fromJson
import one.pkg.modpublish.util.io.JsonParser.toJson
import one.pkg.modpublish.util.io.VersionProcessor
import one.pkg.modpublish.util.metadata.ModVersion.extractVersionNumber
import one.pkg.modpublish.util.resources.LocalResources
import one.pkg.modpublish.util.version.constraint.VersionConstraint
import one.pkg.modpublish.util.version.constraint.VersionConstraintParser
import org.intellij.plugins.markdown.lang.MarkdownFileType
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.CompletionException
import javax.swing.*

class PublishModDialog(
    project: Project?,
    private var jarFiles: Array<VirtualFile>
) : BaseDialogWrapper(project, true) {

    private val modTypes: Map<VirtualFile, List<ModType>> =
        jarFiles.associateWith { ModType.getAll(it) }

    private var modInfo: LocalModInfo? = null
    private var parser: VersionConstraint? = null
    private var updateVersionList = false

    // UI Components
    private lateinit var versionNameField: JBTextField
    private lateinit var versionNumberField: JBTextField
    private lateinit var githubCheckBox: JBCheckBox
    private lateinit var modrinthCheckBox: JBCheckBox
    private lateinit var curseforgeCheckBox: JBCheckBox
    private lateinit var clientCheckBox: JBCheckBox
    private lateinit var serverCheckBox: JBCheckBox
    private lateinit var releaseType: JComboBox<ReleaseChannel>
    private lateinit var primaryFile: JComboBox<VirtualFile>
    private lateinit var loaderCheckBoxes: List<JBCheckBox>
    private lateinit var minecraftVersionList: JBList<MinecraftVersionItem>
    private lateinit var minecraftVersionModel: DefaultListModel<MinecraftVersionItem>
    private lateinit var showSnapshotsCheckBox: JBCheckBox
    private lateinit var changelogField: EditorTextField
    private lateinit var dependencyPanel: DependencyManagerPanel

    private var minecraftVersions: List<MinecraftVersion>? = null
    private lateinit var supportedInfo: SupportedInfo

    init {
        updateParser(jarFiles.first())

        setTitle("title.publish", jarFiles.first().name)
        isModal = true

        loadConfigData()
        init()
        setOKButtonText(get("button.publish"))
        setCancelButtonText(get("button.cancel"))
        setOKButtonDefault()
    }

    private fun updateParser(primaryFile: VirtualFile) {
        parser = modTypes[primaryFile]
            ?.firstOrNull { it != ModType.Rift }
            ?.getMod(primaryFile)
            ?.versionRange
            ?.takeIf { it.isNotEmpty() }
            ?.let { runCatching { VersionConstraintParser.parse(it) }.getOrNull() }
    }

    private fun loadConfigData() {
        supportedInfo = LocalResources.getSupportedInfo()
    }

    private fun updateJarFiles(current: VirtualFile) {
        jarFiles = arrayOf(current) + jarFiles.filterNot { it == current }
    }

    private fun onPrimaryFileUpdate() {
        val current = primaryFile.selectedItem as VirtualFile
        updateJarFiles(current)

        val types = modTypes[current].orEmpty()
        loaderCheckBoxes.forEach { it.isSelected = types.contains(ModType.of(it.text.lowercase())) }

        updateParser(current)
        loadModInfo(current)
        updateMinecraftVersions()
        setTitle("title.publish", current.name)
    }

    override fun createCenterPanel(): JComponent {
        val formBuilder = FormBuilder.createFormBuilder()

        // Version name and number
        versionNameField = JBTextField()
        versionNumberField = JBTextField()
        formBuilder.addLabeledComponent(get("component.name.version-name"), versionNameField)
        formBuilder.addLabeledComponent(get("component.name.version-number"), versionNumberField)

        // Publish targets
        githubCheckBox = JBCheckBox("GitHub")
        modrinthCheckBox = JBCheckBox("Modrinth")
        curseforgeCheckBox = JBCheckBox("CurseForge")

        formBuilder.addLabeledComponent(
            get("component.name.targets"),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(githubCheckBox)
                add(modrinthCheckBox)
                add(curseforgeCheckBox)
            }
        )

        // Support targets
        clientCheckBox = JBCheckBox(get("dialog.modpublish.publish.support.client"))
        serverCheckBox = JBCheckBox(get("dialog.modpublish.publish.support.server"))

        formBuilder.addLabeledComponent(
            get("dialog.modpublish.publish.support.title"),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(clientCheckBox)
                add(serverCheckBox)
            }
        )

        // Loaders
        loaderCheckBoxes = ModType.valuesList.map { launcher ->
            JBCheckBox(launcher.displayName).apply {
                isSelected = modTypes.values.first().contains(launcher)
            }
        }

        formBuilder.addLabeledComponent(
            get("component.name.loaders"),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply { loaderCheckBoxes.forEach(::add) }
        )

        // Primary file
        primaryFile = ComboBox(jarFiles).apply {
            addActionListener { onPrimaryFileUpdate() }
            renderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean
                ): Component {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    if (value is VirtualFile) text = value.name
                    return this
                }
            }
        }
        formBuilder.addLabeledComponent(
            get("component.name.primary-file"),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(primaryFile) })

        // Release type
        releaseType = ComboBox(ReleaseChannel.entries.toTypedArray())
        formBuilder.addLabeledComponent(
            get("component.name.release-channel"),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(releaseType) })

        // Minecraft versions
        minecraftVersionModel = DefaultListModel()
        minecraftVersionList = JBList(minecraftVersionModel).apply {
            cellRenderer = CheckBoxListCellRenderer()
            selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    locationToIndex(e.point).takeIf { it != -1 }?.let { index ->
                        val item = minecraftVersionModel.getElementAt(index)
                        item.selected = !item.selected
                        repaint()
                    }
                }
            })
        }
        showSnapshotsCheckBox = JBCheckBox(get("component.name.snapshot")).apply {
            addActionListener { updateMinecraftVersions() }
        }

        updateMinecraftVersions()

        formBuilder.addPlatformSection(
            get("component.name.mc-version"),
            Icons.Static.ListBar,
            FieldConfig.of {
                JPanel(BorderLayout()).apply {
                    add(JBScrollPane(minecraftVersionList).apply {
                        preferredSize = Dimension(200, 120)
                    }, BorderLayout.CENTER)
                    add(showSnapshotsCheckBox, BorderLayout.SOUTH)
                }
            },
            FieldConfig.of {
                val button1 = JButton(get("component.name.update-version-list")).apply {
                    icon = Icons.Static.Sync
                    toolTipText = get("component.tooltip.update-version-list")
                    addActionListener { _ ->
                        async {
                            isEnabled = false
                            setButtonLoading(this)
                            if (VersionProcessor.updateVersions()) {
                                updateVersionList = true
                                showSuccessDialog("message.update.success", "title.success")
                                updateMinecraftVersions()
                            } else showFailedDialog("message.update.failed", "title.failed")
                            icon = Icons.Static.Sync
                            isEnabled = true
                        }
                    }
                }

                val button2 = JButton(get("component.name.reset-version-list")).apply {
                    icon = Icons.Static.WrenchScrewdriver
                    toolTipText = get("component.tooltip.reset-version-list")
                    addActionListener { _ ->
                        async {
                            FileAPI.getUserDataFile("minecraft.version.json").takeIf { it.exists() }?.delete()
                            showSuccessDialog("message.update.success", "title.success")
                            updateVersionList = true
                            updateMinecraftVersions()
                        }
                    }
                }

                JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                    add(button1, BorderLayout.WEST)
                    add(button2, BorderLayout.CENTER)
                }
            }
        )

        // Changelog
        formBuilder.addPlatformSection(get("component.name.changelog"), Icons.Static.Clipboard, FieldConfig.of {
            changelogField = runCatching {
                EditorTextField("", project, MarkdownFileType.INSTANCE)
            }.getOrElse {
                EditorTextField("", project, PlainTextFileType.INSTANCE)
            }.apply {
                preferredSize = Dimension(500, 150)
                minimumSize = Dimension(500, 100)
                setOneLineMode(false)
            }
            changelogField
        })

        // Dependency manager
        formBuilder.addPlatformSection(
            get("component.name.dependencies"), Icons.Static.Library,
            FieldConfig.of { DependencyManagerPanel(this).also { dependencyPanel = it } })

        autoFillFields()
        return formBuilder.toScrollPanel(800, 650)
    }

    private fun updateMinecraftVersions() {
        SwingUtilities.invokeLater {
            minecraftVersionModel.clear()
            val includeSnapshots = showSnapshotsCheckBox.isSelected

            if (minecraftVersions == null || updateVersionList) {
                updateVersionList = false
                minecraftVersions = LocalResources.getMinecraftVersions()
            }

            minecraftVersions.orEmpty()
                .filter { it.type == "release" || (includeSnapshots && it.type == "snapshot") }
                .forEach { minecraftVersionModel.addElement(MinecraftVersionItem(it, false)) }

            autoFillMinecraftVersions()
        }
    }

    private fun autoFillFields() {
        loadModInfo(jarFiles.first())
        loadPersistedData()
    }

    private fun loadModInfo(current: VirtualFile) {
        val modType = modTypes[current]?.firstOrNull()
        val versionNameFormat = PID.CommonVersionFormat.get(requireNotNull(project))

        val (version, versionName) = modInfo?.let { info ->
            val lowVersion = parser?.lowVersion.orEmpty()
            val highVersion = parser?.maxVersion.orEmpty()
            val v = info.version

            val name = versionNameFormat.takeIf { it.isNotEmpty() }?.run {
                replace("{version}", v)
                    .replace("{name}", info.name)
                    .replace("{loader}", modType?.displayName.orEmpty())
                    .replace("{low-version}", lowVersion)
                    .replace("{high-version}", highVersion)
            } ?: current.nameWithoutExtension

            v to name
        } ?: run {
            val v = current.extractVersionNumber()
            v to current.nameWithoutExtension
        }

        versionNameField.text = versionName
        versionNumberField.text = version
    }

    private fun autoFillMinecraftVersions() {
        if (minecraftVersionModel.size == 0) return

        val indicesToSelect = listOfNotNull(parser?.lowVersion, parser?.maxVersion)
            .mapNotNull { version ->
                (0 until minecraftVersionModel.size).firstOrNull { i ->
                    minecraftVersionModel.getElementAt(i).version.version == version
                }
            }
            .let { found ->
                when (found.size) {
                    2 -> (minOf(found[0], found[1])..maxOf(found[0], found[1]))
                    1 -> 0..found[0]
                    else -> listOf(0)
                }
            }

        indicesToSelect.toList().onEach { idx ->
            minecraftVersionModel.getElementAt(idx).selected = true
        }

        minecraftVersionList.repaint()
    }


    private fun JBCheckBox.setFailedSelect() {
        isEnabled = false
        setErrorStyle()
        toolTipText = get("tooltip.decrypt.failed")
    }

    private fun loadPersistedData() {
        val properties = PropertiesComponent.getInstance(requireNotNull(project))
        val p2 = Properties.getProperties(project)

        if (!p2.modrinth.isEnabled()) {
            modrinthCheckBox.isEnabled = false
            modrinthCheckBox.toolTipText = get("tooltip.modrinth.disable")
        } else if (p2.modrinth.token.failed) {
            modrinthCheckBox.setFailedSelect()
        }

        if (!p2.curseforge.isEnabled()) {
            curseforgeCheckBox.isEnabled = false
            curseforgeCheckBox.toolTipText = get("tooltip.curseforge.disable")
        } else if (p2.curseforge.token.failed || p2.curseforge.studioToken.failed) {
            curseforgeCheckBox.setFailedSelect()
        }

        if (!p2.github.isEnabled()) {
            githubCheckBox.isEnabled = false
            githubCheckBox.toolTipText = get("tooltip.git.disable", "Github")
        } else if (p2.github.token.failed) {
            githubCheckBox.setFailedSelect()
        }

        changelogField.text = properties.getValue("modpublish.changelog", "")

        val savedDependenciesJson = properties.getValue("modpublish.dependencies", "[]")
        val savedDependencies: List<DependencyInfo> = try {
            savedDependenciesJson.fromJson(LocalResources.dpType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        dependencyPanel.setDependencies(savedDependencies.toMutableList())
    }

    private fun savePersistedData() {
        val properties = PropertiesComponent.getInstance(requireNotNull(project))
        properties.setValue("modpublish.changelog", changelogField.text)
        val dependencies = dependencyPanel.getDependencies()
        val dependenciesJson = dependencies.toJson()
        properties.setValue("modpublish.dependencies", dependenciesJson)
    }

    private fun doOKActionFirst(): PublishResult? {
        var validationResult = validatePublishTargetSelection()
        if (validationResult != null) return validationResult

        validationResult = validateModrinthCurseforgeSettings()
        if (validationResult != null) return validationResult

        return validateVersionFields()
    }

    private fun validatePublishTargetSelection(): PublishResult? {
        if (!curseforgeCheckBox.isSelected && !modrinthCheckBox.isSelected && !githubCheckBox.isSelected) {
            return PublishResult.of("failed.1")
        }
        return null
    }

    private fun validateModrinthCurseforgeSettings(): PublishResult? {
        val hasModrinthOrCurseforge = modrinthCheckBox.isSelected || curseforgeCheckBox.isSelected

        if (hasModrinthOrCurseforge && !clientCheckBox.isSelected && !serverCheckBox.isSelected) {
            return PublishResult.of("failed.2")
        }

        if (hasModrinthOrCurseforge && loaderCheckBoxes.none { it.isSelected }) {
            return PublishResult.of("failed.3")
        }
        return null
    }

    private fun validateVersionFields(): PublishResult? {
        if (versionNameField.text.isNullOrBlank()) {
            return PublishResult.of("failed.5")
        }
        if (versionNumberField.text.isNullOrBlank()) {
            return PublishResult.of("failed.6")
        }
        return null
    }

    override fun doOKAction() {
        doOKActionFirst()?.let {
            showFailedDialogRaw(get("message.failed", it.result!!), get("title.failed"))
            return
        }

        okAction.isEnabled = false
        setOKButtonLoading()
        setOKButtonText(get("button.publishing"))

        async {
            savePersistedData()
            val publishData = collectPublishData()
            val result = performPublish(publishData)

            val (isOk, failureMessage) = result.fold(true to "") { acc, r ->
                val (_, msg) = acc
                if (r.isFailure) {
                    val newMsg = if (r.id.isBlank()) r.result!! else ("$msg\n${r.id}: ${r.result!!}")
                    false to newMsg
                } else {
                    acc
                }
            }

            async {
                setOKButtonDefault()
                okAction.isEnabled = true
                setOKButtonText(get("button.publish"))

                if (isOk) {
                    super.doOKAction()
                    showSuccessDialog("message.success", "title.success")
                    close(0, true)
                } else {
                    showFailedDialogRaw(get("message.failed", failureMessage), get("title.failed"))
                }
            }
        }
    }

    private fun collectPublishData(): PublishData {
        val selectedLoaders = loaderCheckBoxes
            .mapIndexedNotNull { index, checkBox -> if (checkBox.isSelected) ModType.valuesList[index] else null }

        val selectedMinecraftVersions = (0 until minecraftVersionModel.size)
            .mapNotNull { i -> minecraftVersionModel.getElementAt(i).takeIf { it.selected }?.version }

        val rT = releaseType.getItemAt(releaseType.selectedIndex)

        supportedInfo.client.enabled = clientCheckBox.isSelected
        supportedInfo.server.enabled = serverCheckBox.isSelected

        val files = jarFiles.map { it.toFile() }.toTypedArray()

        return PublishData(
            versionNameField.text,
            versionNumberField.text,
            getPublishTargets(),
            rT,
            selectedLoaders,
            supportedInfo,
            selectedMinecraftVersions,
            changelogField.text,
            dependencyPanel.getDependencies(),
            files
        )
    }

    private fun performPublish(data: PublishData): List<PublishResult> {
        val results = mutableListOf<PublishResult>()
        if (data.minecraftVersions.isEmpty()) {
            results.add(PublishResult.of("failed.4"))
            return results
        }

        try {
            val curseForgeTask =
                createPublishTask(TargetType.CurseForge.api, curseforgeCheckBox, data)
            val modrinthTask =
                createPublishTask(TargetType.Modrinth.api, modrinthCheckBox, data)
            val githubTask =
                createPublishTask(TargetType.Github.api, githubCheckBox, data)

            runBlocking {
                joinResult(results, curseForgeTask, modrinthTask, githubTask)
            }
        } catch (e: CompletionException) {
            results.add(
                PublishResult.of(
                    "failed.7",
                    e.message ?: "Unknown error"
                )
            )
        }
        return results
    }

    private fun createPublishTask(
        api: API,
        checkBox: JBCheckBox,
        data: PublishData
    ): Deferred<PublishResult>? {
        return if (checkBox.isSelected) {
            Async.rAsync {
                api.createVersion(data, requireNotNull(project))
            }
        } else null
    }

    private suspend fun joinResult(list: MutableList<PublishResult>, vararg futures: Deferred<PublishResult>?) {
        list.addAll(futures.filterNotNull().filterNot { it.isCancelled }.awaitAll())
    }

    fun getPublishTargets(): Selector {
        return Selector.of(modrinthCheckBox, curseforgeCheckBox, githubCheckBox)
    }
}