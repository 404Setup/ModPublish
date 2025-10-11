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
import one.pkg.modpublish.settings.ModPublishSettings
import one.pkg.modpublish.util.protect.HardwareFingerprint
import one.pkg.modpublish.util.protect.Protect

object Properties {
    fun getPropertiesComponent(project: Project): PropertiesComponent {
        return PropertiesComponent.getInstance(project)
    }

    fun getProtectValue(properties: PropertiesComponent, dataKey: PID): Info {
        val v = dataKey.get(properties)
        if (v.isBlank()) {
            val state = requireNotNull(ModPublishSettings.getInstance().state)
            return when (dataKey) {
                PID.ModrinthToken -> state.getModrinthToken()
                PID.CurseForgeToken -> state.getCurseforgeToken()
                PID.CurseForgeStudioToken -> state.getCurseforgeStudioToken()
                PID.GithubToken -> state.getGithubToken()
                PID.GitlabToken -> state.getGitlabToken()
                else -> Info.INSTANCE
            }
        }
        val r = Protect.decryptString(v, HardwareFingerprint.secureProjectKey)
        return if (r.isBlank() || r == v) {
            Info.of(data = r, failed = true, globalData = false)
        } else {
            Info.of(r)
        }
    }

    fun setProtectValue(properties: PropertiesComponent, dataKey: String, data: String) {
        if (dataKey.isBlank()) return
        properties.setValue(
            dataKey,
            if (data.isBlank()) null else Protect.encryptString(data, HardwareFingerprint.secureProjectKey)
        )
    }

    fun getProperties(project: Project): Property {
        return Property.getInstance(getPropertiesComponent(project))
    }
}
