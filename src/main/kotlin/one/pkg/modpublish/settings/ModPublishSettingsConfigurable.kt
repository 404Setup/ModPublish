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
package one.pkg.modpublish.settings

import com.intellij.openapi.options.Configurable
import one.pkg.modpublish.util.resources.Lang
import java.util.*
import javax.swing.JComponent

class ModPublishSettingsConfigurable : Configurable {
    private var component: ModPublishSettingsComponent? = null

    override fun getDisplayName(): String =
        Lang.get("title.global-settings")

    override fun getPreferredFocusedComponent(): JComponent? =
        component?.preferredFocusedComponent

    override fun createComponent(): JComponent? {
        component = ModPublishSettingsComponent()
        return component?.panel
    }

    override fun isModified(): Boolean {
        val state = Objects.requireNonNull(ModPublishSettings.getInstance().state)
        val c = component ?: return false

        return c.modrinthTokenTextValue != state.getModrinthToken().data ||
                c.curseforgeTokenTextValue != state.getCurseforgeToken().data ||
                c.curseforgeStudioTokenTextValue != state.getCurseforgeStudioToken().data ||
                c.githubTokenTextValue != state.getGithubToken().data ||
                c.isAutoProxyEnabled != state.autoProxy ||
                c.proxyType != state.proxyType ||
                c.proxyAddress != state.proxyAddress ||
                c.proxyPort != state.proxyPort ||
                c.proxyUsername != state.proxyUsername ||
                c.proxyPassword != state.proxyPassword ||
                c.isNetworkEnableSSLCheck != state.networkEnableSSLCheck ||
                c.networkConnectTimeout != state.networkConnectTimeout ||
                c.networkReadTimeout != state.networkReadTimeout ||
                c.networkWriteTimeout != state.networkWriteTimeout
    }

    override fun apply() {
        val state = Objects.requireNonNull(ModPublishSettings.getInstance().state)
        val c = component ?: return

        state.updateModrinthToken(c.modrinthTokenTextValue)
        state.updateCurseforgeToken(c.curseforgeTokenTextValue)
        state.updateCurseforgeStudioToken(c.curseforgeStudioTokenTextValue)
        state.updateGithubToken(c.githubTokenTextValue)
        state.autoProxy = c.isAutoProxyEnabled
        state.proxyType = c.proxyType
        state.proxyAddress = c.proxyAddress
        state.proxyPort = c.proxyPort
        state.proxyUsername = c.proxyUsername
        state.proxyPassword = c.proxyPassword
        state.networkEnableSSLCheck = c.isNetworkEnableSSLCheck
        state.updateNetworkConnectTimeout(c.networkConnectTimeout)
        state.updateNetworkReadTimeout(c.networkReadTimeout)
        state.updateNetworkWriteTimeout(c.networkWriteTimeout)
    }

    override fun reset() {
        val state = Objects.requireNonNull(ModPublishSettings.getInstance().state)
        val c = component ?: return

        c.modrinthTokenTextValue = state.getModrinthToken().data
        c.curseforgeTokenTextValue = state.getCurseforgeToken().data
        c.curseforgeStudioTokenTextValue = state.getCurseforgeStudioToken().data
        c.githubTokenTextValue = state.getGithubToken().data
        c.isAutoProxyEnabled = state.autoProxy
        c.proxyType = state.proxyType
        c.proxyAddress = state.proxyAddress
        c.proxyPort = state.proxyPort
        c.proxyUsername = state.proxyUsername
        c.proxyPassword = state.proxyPassword
        c.isNetworkEnableSSLCheck = state.networkEnableSSLCheck
        c.networkConnectTimeout = state.networkConnectTimeout
        c.networkReadTimeout = state.networkReadTimeout
        c.networkWriteTimeout = state.networkWriteTimeout
    }

    override fun disposeUIResources() {
        component = null
    }
}
