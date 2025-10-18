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
package one.pkg.modpublish.settings.properties

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import one.pkg.modpublish.data.internal.Info
import javax.swing.JTextField

@Suppress("UNUSED")
enum class PID(val id: String, val protect: Boolean) {
    ModrinthModID("modpublish.modrinth.modid", false),
    ModrinthToken("modpublish.modrinth.token", true),
    CurseForgeModID("modpublish.curseforge.modid", false),
    CurseForgeToken("modpublish.curseforge.token", true),
    CurseForgeStudioToken("modpublish.curseforge.studioToken", true),
    GithubToken("modpublish.github.token", true),
    GithubRepo("modpublish.github.repo", false),
    GithubBranch("modpublish.github.branch", false),
    GitlabToken("modpublish.gitlab.token", true),
    GitlabRepo("modpublish.gitlab.repo", false),
    GitlabBranch("modpublish.gitlab.branch", false),
    GenerateForgeUpdateEnabled("modpublish.generate-forge-update.enabled", false),
    GenerateForgeUpdateSplit("modpublish.generate-forge-update.split", false),
    CommonVersionFormat("modpublish.common.versionFormat", false),;

    fun get(project: Project): String {
        return get(Properties.getPropertiesComponent(project))
    }

    fun get(properties: PropertiesComponent): String {
        return properties.getValue(id, "")
    }

    fun getProtect(project: Project): Info {
        return getProtect(PropertiesComponent.getInstance(project))
    }

    fun getProtect(properties: PropertiesComponent): Info {
        return Properties.getProtectValue(properties, this)
    }

    fun set(project: Project, data: String) {
        set(PropertiesComponent.getInstance(project), data)
    }

    fun set(properties: PropertiesComponent, data: String) {
        if (protect) Properties.setProtectValue(properties, id, data)
        else properties.setValue(id, data)
    }

    fun set(project: Project, component: JTextField) {
        set(project, component.getText())
    }

    fun set(properties: PropertiesComponent, component: JTextField) {
        set(properties, component.getText())
    }
}
