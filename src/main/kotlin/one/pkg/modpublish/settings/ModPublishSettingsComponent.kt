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

import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import one.pkg.modpublish.ui.base.BaseDialogWrapper
import one.pkg.modpublish.ui.base.FieldConfig
import one.pkg.modpublish.ui.icon.Icons
import one.pkg.modpublish.util.protect.Protect
import java.net.Proxy
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

@Suppress("UNUSED")
class ModPublishSettingsComponent : BaseDialogWrapper(null) {
    val panel: JPanel

    private lateinit var modrinthTokenText: JBTextField
    private lateinit var modrinthTokenLink: ActionLink

    private lateinit var curseforgeTokenText: JBTextField
    private lateinit var curseforgeStudioTokenText: JBTextField
    private lateinit var curseforgeTokenLink: ActionLink
    private lateinit var curseforgeStudioTokenLink: ActionLink

    private lateinit var githubTokenText: JBTextField
    private lateinit var githubTokenLink: ActionLink

    private lateinit var gitlabTokenText: JBTextField
    private lateinit var gitlabTokenLink: ActionLink

    private lateinit var autoProxyCheckBox: JBCheckBox
    private lateinit var proxyTypeComboBox: JComboBox<Proxy.Type>
    private lateinit var proxyAddressText: JBTextField
    private lateinit var proxyPortText: JBTextField
    private lateinit var proxyUsernameText: JBTextField
    private lateinit var proxyPasswordText: JBTextField

    private lateinit var networkEnableSSLCheckBox: JBCheckBox
    private lateinit var networkConnectTimeoutText: JBTextField
    private lateinit var networkReadTimeoutText: JBTextField
    private lateinit var networkWriteTimeoutText: JBTextField

    init {
        val formBuilder = FormBuilder.createFormBuilder()

        formBuilder.addPlatformSection(
            "Modrinth", Icons.Target.Modrinth,
            FieldConfig.of("Token") { JBTextField().also { modrinthTokenText = it } },
            FieldConfig.of {
                createActionLink(
                    "Create Modrinth Token",
                    "https://modrinth.com/settings/pats"
                ).also { modrinthTokenLink = it }
            }
        )

        formBuilder.addPlatformSection(
            "CurseForge", Icons.Target.CurseForge,
            FieldConfig.of("Token") { JBTextField().also { curseforgeTokenText = it } },
            FieldConfig.of("Studio Token") { JBTextField().also { curseforgeStudioTokenText = it } },
            FieldConfig.of {
                createActionLink(
                    "Create CurseForge Studio Token",
                    "https://console.curseforge.com/?#/api-keys"
                ).also { curseforgeStudioTokenLink = it }
            },
            FieldConfig.of {
                createActionLink(
                    "Create CurseForge Token",
                    "https://legacy.curseforge.com/account/api-tokens"
                ).also { curseforgeTokenLink = it }
            }
        )

        formBuilder.addPlatformSection(
            "GitHub", Icons.Target.Github,
            FieldConfig.of("Token") { JBTextField().also { githubTokenText = it } },
            FieldConfig.of {
                createActionLink(
                    "Create GitHub Token",
                    "https://github.com/settings/personal-access-tokens"
                ).also { githubTokenLink = it }
            }
        )

        formBuilder.addPlatformSection(
            "GitLab", Icons.Target.Gitlab,
            FieldConfig.of("Token") { JBTextField().also { gitlabTokenText = it } },
            FieldConfig.of {
                createActionLink(
                    "Create GitLab Token",
                    "https://gitlab.com/-/user_settings/personal_access_tokens"
                ).also { gitlabTokenLink = it }
            }
        )

        formBuilder.addPlatformSection(
            get("setting.network.name"), Icons.Static.DataBar,
            FieldConfig.of(get("setting.network.ssl-check.name")) {
                JBCheckBox().also {
                    networkEnableSSLCheckBox = it
                }
            },
            FieldConfig.of { createLabel(get("setting.network.ssl-check.desc")) },
            FieldConfig.of(get("setting.network.read-timeout.name")) {
                JBTextField().intField(1, Int.MAX_VALUE.toLong()).also {
                    it.toolTipText = get("setting.network.read-timeout.desc")
                    networkReadTimeoutText = it
                }
            },
            FieldConfig.of(get("setting.network.write-timeout.name")) {
                JBTextField().intField(1, Int.MAX_VALUE.toLong()).also {
                    it.toolTipText = get("setting.network.write-timeout.desc")
                    networkWriteTimeoutText = it
                }
            },
            FieldConfig.of(get("setting.network.connect-timeout.name")) {
                JBTextField().intField(1, Int.MAX_VALUE.toLong()).also {
                    it.toolTipText = get("setting.network.connect-timeout.desc")
                    networkConnectTimeoutText = it
                }
            }
        )

        formBuilder.addPlatformSection(
            get("setting.proxy.name"), Icons.Static.Globe,
            FieldConfig.of { createLabel(get("tips.2")) },
            FieldConfig.of(get("setting.proxy.auto-proxy")) { JBCheckBox().also { autoProxyCheckBox = it } },
            FieldConfig.of(get("setting.proxy.type")) {
                JComboBox(
                    arrayOf(
                        Proxy.Type.SOCKS,
                        Proxy.Type.HTTP
                    )
                ).also { proxyTypeComboBox = it }
            },
            FieldConfig.of(get("setting.proxy.address")) { JBTextField().also { proxyAddressText = it } },
            FieldConfig.of(get("setting.proxy.port")) {
                JBTextField().intField(1, 65535).also {
                    proxyPortText = it
                }
            },
            FieldConfig.of(get("setting.proxy.user")) { JBTextField().also { proxyUsernameText = it } },
            FieldConfig.of(get("setting.proxy.pass")) { JBTextField().also { proxyPasswordText = it } }
        )

        panel = formBuilder.addComponentFillVertically(JPanel(), 0).panel
    }

    override fun createCenterPanel(): JComponent? = null

    override fun getPreferredFocusedComponent(): JComponent = modrinthTokenText

    var modrinthTokenTextValue: String
        get() = Protect.decryptString(modrinthTokenText.text)
        set(newText) {
            modrinthTokenText.text = newText
        }

    var curseforgeTokenTextValue: String
        get() = Protect.decryptString(curseforgeTokenText.text)
        set(newText) {
            curseforgeTokenText.text = newText
        }

    var curseforgeStudioTokenTextValue: String
        get() = Protect.decryptString(curseforgeStudioTokenText.text)
        set(newText) {
            curseforgeStudioTokenText.text = newText
        }

    var githubTokenTextValue: String
        get() = Protect.decryptString(githubTokenText.text)
        set(newText) {
            githubTokenText.text = newText
        }

    var gitlabTokenTextValue: String
        get() = Protect.decryptString(gitlabTokenText.text)
        set(newText) {
            gitlabTokenText.text = newText
        }

    var isAutoProxyEnabled: Boolean
        get() = autoProxyCheckBox.isSelected
        set(enabled) {
            autoProxyCheckBox.isSelected = enabled
        }

    var proxyType: Int
        get() = proxyTypeComboBox.selectedIndex
        set(newType) {
            proxyTypeComboBox.selectedIndex = newType
        }

    var proxyTypeValue: Proxy.Type
        get() = proxyTypeComboBox.selectedItem as Proxy.Type
        set(newType) {
            proxyTypeComboBox.selectedItem = newType
        }

    var proxyAddress: String
        get() = proxyAddressText.text
        set(newAddress) {
            proxyAddressText.text = newAddress
        }

    var proxyPort: Int
        get() = proxyPortText.text.toInt()
        set(newPort) {
            proxyPortText.text = newPort.toString()
        }

    var proxyUsername: String
        get() = proxyUsernameText.text
        set(newUsername) {
            proxyUsernameText.text = newUsername
        }

    var proxyPassword: String
        get() = proxyPasswordText.text
        set(newPassword) {
            proxyPasswordText.text = newPassword
        }

    var isNetworkEnableSSLCheck: Boolean
        get() = networkEnableSSLCheckBox.isSelected
        set(enabled) {
            networkEnableSSLCheckBox.isSelected = enabled
        }

    var networkConnectTimeout: Int
        get() = networkConnectTimeoutText.text.toInt()
        set(newTimeout) {
            networkConnectTimeoutText.text = newTimeout.toString()
        }

    var networkReadTimeout: Int
        get() = networkReadTimeoutText.text.toInt()
        set(newTimeout) {
            networkReadTimeoutText.text = newTimeout.toString()
        }

    var networkWriteTimeout: Int
        get() = networkWriteTimeoutText.text.toInt()
        set(newTimeout) {
            networkWriteTimeoutText.text = newTimeout.toString()
        }
}
