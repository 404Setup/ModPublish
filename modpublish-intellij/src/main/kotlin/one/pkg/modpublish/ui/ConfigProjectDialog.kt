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
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import one.pkg.modpublish.settings.properties.PID
import one.pkg.modpublish.settings.properties.Properties.getProperties
import one.pkg.modpublish.ui.base.BaseDialogWrapper
import one.pkg.modpublish.ui.base.FieldConfig.Companion.of
import one.pkg.modpublish.ui.icon.Icons
import one.pkg.modpublish.util.resources.Lang.translate
import java.util.*
import javax.swing.JComponent

class ConfigProjectDialog(project: Project) : BaseDialogWrapper(project) {
    private lateinit var commonVersionFormatField: JBTextField

    private lateinit var modrinthTokenField: JBTextField
    private lateinit var modrinthModIDField: JBTextField

    private lateinit var curseforgeTokenField: JBTextField
    private lateinit var curseforgeStudioTokenField: JBTextField
    private lateinit var curseforgeModIDField: JBTextField

    private lateinit var githubTokenField: JBTextField
    private lateinit var githubRepoField: JBTextField
    private lateinit var githubBranchField: JBTextField

    private lateinit var gitlabTokenField: JBTextField
    private lateinit var gitlabRepoField: JBTextField
    private lateinit var gitlabBranchField: JBTextField

    init {
        setTitle("title.config-project", project.name)
        isModal = true

        init()
        setOKButtonText("button.save".translate())
        setCancelButtonText("button.cancel".translate())
    }

    override fun createCenterPanel(): JComponent {
        val formBuilder = FormBuilder.createFormBuilder()

        val token = "dialog.modpublish.config-project.token".translate()
        val studioToken = "dialog.modpublish.config-project.studio-token".translate()
        val modIdLabel = "Mod ID:"
        val repoLabel = "dialog.modpublish.config-project.repo".translate()
        val branchLabel = "dialog.modpublish.config-project.branch".translate()

        formBuilder.addPlatformSection(
            "Common", Icons.Static.Book,
            of("component.name.common.version-format".translate()) {
                createTextField().also {
                    commonVersionFormatField = it
                }
            }
        )

        formBuilder.addPlatformSection(
            "Modrinth", Icons.Target.Modrinth,
            of(token) { createTextField().also { modrinthTokenField = it } },
            of(modIdLabel) { createTextField().also { modrinthModIDField = it } }
        )

        formBuilder.addPlatformSection(
            "CurseForge", Icons.Target.CurseForge,
            of(token) { createTextField().also { curseforgeTokenField = it } },
            of(studioToken) { createTextField().also { curseforgeStudioTokenField = it } },
            of(modIdLabel) { createTextField().also { curseforgeModIDField = it } }
        )

        formBuilder.addPlatformSection(
            "GitHub", Icons.Target.Github,
            of(token) { createTextField().also { githubTokenField = it } },
            of(repoLabel) {
                createTextField().also {
                    it.setToolTipText("dialog.modpublish.config-project.repo.tooltips".translate())
                    githubRepoField = it
                }
            },
            of(branchLabel) {
                createTextField().also {
                    it.setToolTipText("dialog.modpublish.config-project.branch.tooltips".translate())
                    githubBranchField = it
                }
            }
        )

        formBuilder.addPlatformSection(
            "GitLab", Icons.Target.Gitlab,
            of(token) { createTextField().also { gitlabTokenField = it } },
            of(repoLabel) { createTextField().also { gitlabRepoField = it } },
            of(branchLabel) { createTextField().also { gitlabBranchField = it } }
        )

        autoFillFields()

        return formBuilder.toScrollPanel(600, 360)
    }

    override fun doOKAction() {
        savePersistedData()
        close(OK_EXIT_CODE)
    }

    private fun autoFillFields() {
        loadPersistedData()
    }

    private fun loadPersistedData() {
        val p1 = getProperties(Objects.requireNonNull<Project>(project))

        commonVersionFormatField.text = p1.common.versionFormat

        if (p1.modrinth.token.globalData) modrinthTokenField.setToolTipText("dialog.modpublish.config-project.global".translate())
        else modrinthTokenField.text = p1.modrinth.token.data
        modrinthModIDField.text = p1.modrinth.modid

        if (p1.curseforge.token.globalData) {
            curseforgeTokenField.setToolTipText("dialog.modpublish.config-project.global".translate())
        } else curseforgeTokenField.text = p1.curseforge.token.data
        if (p1.curseforge.studioToken.globalData) {
            curseforgeStudioTokenField.setToolTipText("dialog.modpublish.config-project.global".translate())
        } else curseforgeStudioTokenField.text = p1.curseforge.studioToken.data
        curseforgeModIDField.text = p1.curseforge.modid

        githubTokenField.text = if (p1.github.token.globalData) "" else p1.github.token.data
        if (p1.github.token.globalData) githubTokenField.setToolTipText("dialog.modpublish.config-project.global".translate())
        githubRepoField.text = p1.github.repo
        githubBranchField.text = p1.github.branch

        gitlabTokenField.text = if (p1.gitlab.token.globalData) "" else p1.gitlab.token.data
        if (p1.gitlab.token.globalData) gitlabTokenField.setToolTipText("dialog.modpublish.config-project.global".translate())
        gitlabRepoField.text = p1.gitlab.repo
        gitlabBranchField.text = p1.gitlab.branch
    }

    private fun savePersistedData() {
        val properties = PropertiesComponent.getInstance(Objects.requireNonNull<Project>(project))

        PID.CommonVersionFormat.set(properties, commonVersionFormatField)

        PID.ModrinthToken.set(properties, modrinthTokenField)
        PID.ModrinthModID.set(properties, modrinthModIDField)

        PID.CurseForgeToken.set(properties, curseforgeTokenField)
        PID.CurseForgeStudioToken.set(properties, curseforgeStudioTokenField)
        PID.CurseForgeModID.set(properties, curseforgeModIDField)

        PID.GithubToken.set(properties, githubTokenField)
        PID.GithubRepo.set(properties, githubRepoField)
        PID.GithubBranch.set(properties, githubBranchField)

        PID.GitlabToken.set(properties, gitlabTokenField)
        PID.GitlabRepo.set(properties, gitlabRepoField)
        PID.GitlabBranch.set(properties, gitlabBranchField)
    }
}
