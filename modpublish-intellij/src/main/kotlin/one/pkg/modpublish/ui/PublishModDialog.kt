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

import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorTextField
import com.intellij.ui.EditorTextFieldProvider
import com.intellij.ui.HorizontalScrollBarEditorCustomization
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
import one.pkg.modpublish.data.internal.PublishType.Companion.toModTypes
import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.data.local.MinecraftVersion
import one.pkg.modpublish.data.local.SupportedInfo
import one.pkg.modpublish.data.network.modrinth.ModrinthEnvironment
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.settings.properties.Properties
import one.pkg.modpublish.ui.base.BaseDialogWrapper
import one.pkg.modpublish.ui.base.FieldConfig
import one.pkg.modpublish.ui.icon.Icons
import one.pkg.modpublish.ui.panel.DependencyManagerPanel
import one.pkg.modpublish.ui.renderer.CheckBoxListCellRenderer
import one.pkg.modpublish.ui.renderer.JarFilesRenderer
import one.pkg.modpublish.util.io.Async
import one.pkg.modpublish.util.io.Async.async
import one.pkg.modpublish.util.io.FileAPI.getUserDataFile
import one.pkg.modpublish.util.io.FileAPI.toFile
import one.pkg.modpublish.util.io.JsonParser.fromJson
import one.pkg.modpublish.util.io.JsonParser.toJson
import one.pkg.modpublish.util.io.VersionProcessor
import one.pkg.modpublish.util.metadata.ModVersion.extractVersionNumber
import one.pkg.modpublish.util.resources.Lang.translate
import one.pkg.modpublish.util.resources.LocalResources
import one.pkg.modpublish.version.constraint.VersionConstraint
import one.pkg.modpublish.version.constraint.VersionConstraintParser
import org.intellij.plugins.markdown.lang.MarkdownFileType
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class PublishModDialog(
    project: Project?,
    private var jarFiles: Array<VirtualFile>
) : BaseDialogWrapper(project, true) {

    companion object {
        private val LOG = Logger.getInstance(Companion::class.java)
    }

    private val publishTypes: Map<VirtualFile, List<PublishType>> =
        jarFiles.associateWith { it.toModTypes() }

    private var modInfo: LocalModInfo? = null
    private var parser: VersionConstraint? = null

    private lateinit var versionNameField: JBTextField
    private lateinit var versionNumberField: JBTextField
    private lateinit var githubCheckBox: JBCheckBox
    private lateinit var gitlabCheckBox: JBCheckBox
    private lateinit var modrinthCheckBox: JBCheckBox
    private lateinit var curseforgeCheckBox: JBCheckBox
    private lateinit var clientCheckBox: JBCheckBox
    private lateinit var serverCheckBox: JBCheckBox
    private lateinit var releaseType: JComboBox<ReleaseChannel>
    private lateinit var primaryFile: JComboBox<VirtualFile>
    private lateinit var loaderCheckBoxes: List<Pair<PublishType, JBCheckBox>>
    private lateinit var minecraftVersionList: JBList<MinecraftVersionItem>
    private lateinit var minecraftVersionModel: DefaultListModel<MinecraftVersionItem>
    private lateinit var showSnapshotsCheckBox: JBCheckBox
    private lateinit var changelogField: EditorTextField
    private lateinit var dependencyPanel: DependencyManagerPanel
    private lateinit var supportLabel: JLabel
    private lateinit var supportPanel: JPanel
    private lateinit var envLabel: JLabel
    private lateinit var envPanel: JPanel
    private lateinit var modrinthEnvironmentComboBox: ComboBox<ModrinthEnvironment>

    private var minecraftVersions: List<MinecraftVersion>? = null
    private lateinit var supportedInfo: SupportedInfo

    init {
        updateParser(jarFiles.first())

        setTitle("title.publish", jarFiles.first().name)
        isModal = true

        loadConfigData()
        init()
        setOKButtonText("button.publish".translate())
        setCancelButtonText("button.cancel".translate())
        setOKButtonDefault()
    }

    private fun updateParser(primaryFile: VirtualFile) {
        parser = publishTypes[primaryFile]
            ?.firstOrNull { it != PublishType.JavaAgent }
            ?.run { getMod(primaryFile).also { modInfo = it } }
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

    private fun updateSideType() {
        clientCheckBox.isSelected = false
        serverCheckBox.isSelected = false
        modInfo?.let {
            when (it.sideType) {
                SideType.BOTH -> {
                    clientCheckBox.isSelected = true; serverCheckBox.isSelected = true
                }

                SideType.CLIENT -> clientCheckBox.isSelected = true
                SideType.SERVER -> serverCheckBox.isSelected = true
            }
        }
    }

    private fun onPrimaryFileUpdate() {
        val current = primaryFile.selectedItem as VirtualFile
        updateJarFiles(current)

        val types = publishTypes[current].orEmpty()
        loaderCheckBoxes.forEach { it.second.isSelected = types.contains(it.first) }

        updateParser(current)
        loadModInfo(current)
        updateMinecraftVersions()
        setTitle("title.publish", current.name)
    }

    override fun createCenterPanel(): JComponent {
        val formBuilder = FormBuilder.createFormBuilder()

        versionNameField = JBTextField()
        versionNumberField = JBTextField()
        formBuilder.addLabeledComponent("component.name.version-name".translate(), versionNameField)
        formBuilder.addLabeledComponent("component.name.version-number".translate(), versionNumberField)

        val updateVisibility = {
            val onlyModrinth =
                modrinthCheckBox.isSelected && !curseforgeCheckBox.isSelected && !githubCheckBox.isSelected && !gitlabCheckBox.isSelected
            val hasModrinth = modrinthCheckBox.isSelected

            if (onlyModrinth) {
                supportLabel.isVisible = false
                supportPanel.isVisible = false
                clientCheckBox.isSelected = true
                serverCheckBox.isSelected = true
            } else {
                supportLabel.isVisible = true
                supportPanel.isVisible = true
            }

            envLabel.isVisible = hasModrinth
            envPanel.isVisible = hasModrinth
        }

        githubCheckBox = JBCheckBox("GitHub").apply { addActionListener { updateVisibility() } }
        gitlabCheckBox = JBCheckBox("GitLab").apply { addActionListener { updateVisibility() } }
        modrinthCheckBox = JBCheckBox("Modrinth").apply { addActionListener { updateVisibility() } }
        curseforgeCheckBox = JBCheckBox("CurseForge").apply { addActionListener { updateVisibility() } }

        formBuilder.addLabeledComponent(
            "component.name.targets".translate(),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(githubCheckBox)
                add(gitlabCheckBox)
                add(modrinthCheckBox)
                add(curseforgeCheckBox)
            }
        )

        clientCheckBox = JBCheckBox("dialog.modpublish.publish.support.client".translate())
        serverCheckBox = JBCheckBox("dialog.modpublish.publish.support.server".translate())

        supportLabel = createFieldLabel("dialog.modpublish.publish.support.title".translate())
        supportPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(clientCheckBox)
            add(serverCheckBox)
        }
        formBuilder.addLabeledComponent(supportLabel, supportPanel)

        envLabel = createFieldLabel("dialog.modpublish.publish.environment.title".translate())
        modrinthEnvironmentComboBox = ComboBox(ModrinthEnvironment.entries.toTypedArray())
        envPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(modrinthEnvironmentComboBox)
        }
        formBuilder.addLabeledComponent(envLabel, envPanel)

        updateVisibility()

        loaderCheckBoxes = PublishType.valuesList.map { launcher ->
            launcher to JBCheckBox(launcher.displayName).apply {
                isSelected = publishTypes.values.first().contains(launcher)
            }
        }

        formBuilder.addLabeledComponent(
            "component.name.loaders".translate(),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply { loaderCheckBoxes.forEach { add(it.second) } }
        )

        primaryFile = ComboBox(jarFiles).apply {
            addActionListener { onPrimaryFileUpdate() }
            renderer = JarFilesRenderer()
        }
        formBuilder.addLabeledComponent(
            "component.name.primary-file".translate(),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(primaryFile) })

        releaseType = ComboBox(ReleaseChannel.entries.toTypedArray())
        formBuilder.addLabeledComponent(
            "component.name.release-channel".translate(),
            JPanel(FlowLayout(FlowLayout.LEFT)).apply { add(releaseType) })

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
        showSnapshotsCheckBox = JBCheckBox("component.name.snapshot".translate()).apply {
            addActionListener { updateMinecraftVersions() }
        }

        updateMinecraftVersions()

        formBuilder.addPlatformSection(
            "component.name.mc-version".translate(),
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
                val button1 = JButton("component.name.update-version-list".translate()).apply {
                    icon = Icons.Static.Sync
                    toolTipText = "component.tooltip.update-version-list".translate()
                    addActionListener { _ ->
                        async {
                            isEnabled = false
                            setButtonLoading(this)
                            if (runBlocking { VersionProcessor.updateVersions() }) {
                                minecraftVersions = null
                                showSuccessDialog("message.update.success", "title.success")
                                updateMinecraftVersions()
                            } else showFailedDialog("message.update.failed", "title.failed")
                            icon = Icons.Static.Sync
                            isEnabled = true
                        }
                    }
                }

                val button2 = JButton("component.name.reset-version-list".translate()).apply {
                    icon = Icons.Static.WrenchScrewdriver
                    toolTipText = "component.tooltip.reset-version-list".translate()
                    addActionListener { _ ->
                        async {
                            "minecraft.version.json".getUserDataFile().takeIf { it.exists() }?.delete()
                            LocalResources.clearMinecraftVersionsCache()
                            minecraftVersions = null
                            showSuccessDialog("message.update.success", "title.success")
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

        formBuilder.addPlatformSection("component.name.changelog".translate(), Icons.Static.Clipboard, FieldConfig.of {
            EditorTextFieldProvider.getInstance().getEditorField(
                Language.ANY, requireNotNull(project), arrayListOf(HorizontalScrollBarEditorCustomization.ENABLED)
            ).apply {
                fileType = MarkdownFileType.INSTANCE
                preferredSize = Dimension(500, 150)
                minimumSize = Dimension(500, 100)
                setOneLineMode(false)

                //addDocumentListener(LFRenderer())
            }.also { changelogField = it }
        })

        formBuilder.addPlatformSection(
            "component.name.dependencies".translate(), Icons.Static.Library,
            FieldConfig.of { DependencyManagerPanel(this).also { dependencyPanel = it } })

        autoFillFields()
        return formBuilder.toScrollPanel(800, 650)
    }

    private fun updateMinecraftVersions() {
        SwingUtilities.invokeLater {
            minecraftVersionModel.clear()
            val includeSnapshots = showSnapshotsCheckBox.isSelected

            if (minecraftVersions == null) {
                minecraftVersions = LocalResources.getMinecraftVersions()
            }

            minecraftVersions.orEmpty().forEach {
                if (it.type == "release" || (includeSnapshots && it.type == "snapshot")) {
                    minecraftVersionModel.addElement(MinecraftVersionItem(it, false))
                }
            }

            autoFillMinecraftVersions()
        }
    }

    private fun autoFillFields() {
        loadModInfo(jarFiles.first())
        loadPersistedData()
    }

    private fun loadModInfo(current: VirtualFile) {
        LOG.info("Start load mod info: ${current.name}")
        updateSideType()
        val modType = publishTypes[current]?.firstOrNull()
        if (modType != null) LOG.info("Loading mod info: ${modType.name}")
        val versionNameFormat = PID.CommonVersionFormat.get(requireNotNull(project))
        if (versionNameFormat.isNotEmpty()) LOG.info("Loading versionNameFormat: $versionNameFormat")

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
            LOG.info("Using default rules")
            val v = current.extractVersionNumber()
            v to current.nameWithoutExtension
        }

        versionNameField.text = versionName
        versionNumberField.text = version
    }

    private fun autoFillMinecraftVersions() {
        if (minecraftVersionModel.size == 0) return

        val indicesToSelect = parser?.let { p ->
            (0 until minecraftVersionModel.size).filter { i ->
                val v = minecraftVersionModel.getElementAt(i).version.version
                p.satisfies(one.pkg.modpublish.version.Version(v))
            }
        } ?: listOf(0)

        indicesToSelect.toList().onEach { idx ->
            minecraftVersionModel.getElementAt(idx).selected = true
        }

        minecraftVersionList.repaint()
    }


    private fun JBCheckBox.setFailedSelect() {
        isEnabled = false
        setErrorStyle()
        toolTipText = "tooltip.decrypt.failed".translate()
    }

    private fun loadPersistedData() {
        val properties = PropertiesComponent.getInstance(requireNotNull(project))
        val p2 = Properties.getProperties(project)

        if (!p2.modrinth.isEnabled()) {
            modrinthCheckBox.isEnabled = false
            modrinthCheckBox.toolTipText = "tooltip.modrinth.disable".translate()
        } else if (p2.modrinth.token.failed) {
            modrinthCheckBox.setFailedSelect()
        }

        if (!p2.curseforge.isEnabled()) {
            curseforgeCheckBox.isEnabled = false
            curseforgeCheckBox.toolTipText = "tooltip.curseforge.disable".translate()
        } else if (p2.curseforge.token.failed) {
            curseforgeCheckBox.setFailedSelect()
        }

        if (!p2.github.isEnabled()) {
            githubCheckBox.isEnabled = false
            githubCheckBox.toolTipText = "tooltip.git.disable".translate("Github")
        } else if (p2.github.token.failed) {
            githubCheckBox.setFailedSelect()
        }

        if (!p2.gitlab.isEnabled()) {
            gitlabCheckBox.isEnabled = false
            gitlabCheckBox.toolTipText = "tooltip.git.disable".translate("Gitlab")
        } else if (p2.gitlab.token.failed) {
            gitlabCheckBox.setFailedSelect()
        }

        changelogField.text = properties.getValue("modpublish.changelog", "").replace("\r\n", "\n").replace("\r", "\n")

        val savedDependenciesJson = properties.getValue("modpublish.dependencies", "[]")
        val savedDependencies: List<DependencyInfo> = try {
            savedDependenciesJson.fromJson(LocalResources.dpType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        dependencyPanel.setDependencies(savedDependencies.toMutableList())

        val savedEnv = properties.getValue("modpublish.environment", "")
        if (savedEnv.isNotEmpty()) {
            ModrinthEnvironment.entries.find { it.id == savedEnv }?.let {
                modrinthEnvironmentComboBox.selectedItem = it
            }
        }
    }

    private fun savePersistedData() {
        val properties = PropertiesComponent.getInstance(requireNotNull(project))
        properties.setValue("modpublish.changelog", changelogField.text)
        val dependencies = dependencyPanel.getDependencies()
        val dependenciesJson = dependencies.toJson()
        properties.setValue("modpublish.dependencies", dependenciesJson)
        val env = modrinthEnvironmentComboBox.selectedItem as? ModrinthEnvironment
        properties.setValue("modpublish.environment", env?.id ?: "")
    }

    private fun doOKActionFirst(): PublishResult? {
        var validationResult = validatePublishTargetSelection()
        if (validationResult != null) return validationResult

        validationResult = validateModrinthCurseforgeSettings()
        if (validationResult != null) return validationResult

        return validateVersionFields()
    }

    private fun validatePublishTargetSelection(): PublishResult? {
        if (!curseforgeCheckBox.isSelected && !modrinthCheckBox.isSelected && !githubCheckBox.isSelected && !gitlabCheckBox.isSelected) {
            return PublishResult.of("failed.1")
        }
        return null
    }

    private fun validateModrinthCurseforgeSettings(): PublishResult? {
        val hasModrinthOrCurseforge = modrinthCheckBox.isSelected || curseforgeCheckBox.isSelected

        if (hasModrinthOrCurseforge && !clientCheckBox.isSelected && !serverCheckBox.isSelected) {
            return PublishResult.of("failed.2")
        }

        if (hasModrinthOrCurseforge && loaderCheckBoxes.none { it.second.isSelected }) {
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
            showFailedDialogRaw("message.failed".translate(it.result!!), "title.failed".translate())
            return
        }

        okAction.isEnabled = false
        setOKButtonLoading()
        setOKButtonText("button.publishing".translate())

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
                setOKButtonText("button.publish".translate())

                if (isOk) {
                    showSuccessDialog("message.success", "title.success")
                    super.doOKAction()
                } else {
                    showFailedDialogRaw("message.failed".translate(failureMessage), "title.failed".translate())
                }
            }
        }
    }

    private fun collectPublishData(): PublishData {
        val selectedLoaders = loaderCheckBoxes
            .mapNotNull { if (it.second.isSelected) it.first else null }

        val selectedMinecraftVersions = (0 until minecraftVersionModel.size)
            .mapNotNull { i -> minecraftVersionModel.getElementAt(i).takeIf { it.selected }?.version }

        val rT = releaseType.getItemAt(releaseType.selectedIndex)

        supportedInfo.client.enabled = clientCheckBox.isSelected
        supportedInfo.server.enabled = serverCheckBox.isSelected

        val files = Array(jarFiles.size) { jarFiles[it].toFile() }

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
            modrinthEnvironmentComboBox.selectedItem as? ModrinthEnvironment,
            files
        )
    }

    private fun performPublish(data: PublishData): List<PublishResult> {
        val results = mutableListOf<PublishResult>()
        if (data.minecraftVersions.isEmpty()) {
            results.add(PublishResult.of("failed.4"))
            return results
        }

        runCatching {
            val curseForgeTask =
                createPublishTask(PublishTarget.CurseForge.api, curseforgeCheckBox, data)
            val modrinthTask =
                createPublishTask(PublishTarget.Modrinth.api, modrinthCheckBox, data)
            val githubTask =
                createPublishTask(PublishTarget.Github.api, githubCheckBox, data)
            val gitlabTask =
                createPublishTask(PublishTarget.Gitlab.api, gitlabCheckBox, data)

            runBlocking {
                joinResult(results, curseForgeTask, modrinthTask, githubTask, gitlabTask)
            }
        }.onFailure { results.add(PublishResult.of("failed.7", it.message ?: "Unknown error")) }
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
        return Selector.of(modrinthCheckBox, curseforgeCheckBox, githubCheckBox, gitlabCheckBox)
    }
}
