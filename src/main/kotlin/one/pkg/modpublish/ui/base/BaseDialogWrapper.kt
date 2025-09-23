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
package one.pkg.modpublish.ui.base

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.components.*
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import one.pkg.modpublish.ui.icon.Icons
import one.pkg.modpublish.util.resources.Lang
import org.jetbrains.annotations.PropertyKey
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.text.AbstractDocument

@Suppress("unused")
abstract class BaseDialogWrapper(
    val project: Project? = null,
    canBeParent: Boolean = false
) : DialogWrapper(project, canBeParent) {

    fun FormBuilder.toScrollPanel(width: Int, height: Int): JComponent {
        val panel = panel.apply {
            border = JBUI.Borders.empty(20, 20, 15, 20)
        }

        return JBScrollPane(panel).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            preferredSize = Dimension(width, height)
            verticalScrollBar.unitIncrement = 16
        }
    }

    fun createFieldLabel(text: String): JLabel = JLabel(text).apply {
        font = UIUtil.getFont(UIUtil.FontSize.NORMAL, null)
    }

    fun createTextField(): JBTextField = JBTextField().apply {
        preferredSize = Dimension(250, preferredSize.height)
    }

    fun JBTextField.intField(
        min: Long = Long.MIN_VALUE,
        max: Long = Long.MAX_VALUE,
        initial: Long? = null
    ): JBTextField {
        val filter = UniversalNumericDocumentFilter(
            minValue = min.toDouble(),
            maxValue = max.toDouble(),
            allowDecimal = false
        )
        (this.document as AbstractDocument).documentFilter = filter
        initial?.let { this.text = it.toString() }
        return this
    }

    fun JBTextField.decimalField(
        min: Double = Double.NEGATIVE_INFINITY,
        max: Double = Double.POSITIVE_INFINITY,
        decimalPlaces: Int = 2,
        initial: Double? = null
    ): JBTextField {
        val filter = UniversalNumericDocumentFilter(
            minValue = min,
            maxValue = max,
            allowDecimal = true,
            decimalPlaces = decimalPlaces
        )
        (this.document as AbstractDocument).documentFilter = filter
        initial?.let { this.text = it.toString() }
        return this
    }

    fun JBTextField.default(value: Number): JBTextField {
        this.text = value.toString()
        return this
    }

    fun createLabel(text: String): JBLabel = JBLabel("<html><small>$text</small></html>").apply {
        foreground = JBColor.GRAY
    }

    fun createActionLink(text: String, url: String): ActionLink =
        ActionLink(text).apply { addActionListener { BrowserUtil.browse(url) }; setExternalLinkIcon() }

    fun createSectionLabel(text: String, icon: Icon? = null): JLabel = JLabel(text).apply {
        font = UIUtil.getFont(UIUtil.FontSize.NORMAL, null).deriveFont(Font.BOLD)
        foreground = UIUtil.getLabelForeground()
        this.icon = icon
    }

    fun JBCheckBox.setErrorStyle() {
        val errorPanel = JPanel(BorderLayout()).apply {
            background = JBColor(Color(255, 200, 200), Color(255, 200, 200))
            border = BorderFactory.createLineBorder(JBColor.RED, 2)
        }
        parent?.let { parent ->
            val idx = parent.components.indexOf(this)
            if (idx >= 0) {
                parent.remove(this)
                errorPanel.add(this, BorderLayout.CENTER)
                parent.add(errorPanel, idx)
                parent.revalidate()
                parent.repaint()
            }
        }
    }

    override fun setTitle(@PropertyKey(resourceBundle = Lang.FILE) key: String) =
        super.setTitle(get(key))

    fun setTitle(@PropertyKey(resourceBundle = Lang.FILE) key: String, vararg params: Any) =
        super.setTitle(get(key, *params))

    fun getJBLabel(@PropertyKey(resourceBundle = Lang.FILE) key: String) = JBLabel(get(key))

    fun getJBCheckBox(@PropertyKey(resourceBundle = Lang.FILE) key: String) =
        getJBCheckBoxRaw(get(key))

    fun getJBCheckBoxRaw(key: String) = getJBCheckBox().apply { text = key }

    fun getJBCheckBox(): JBCheckBox = JBCheckBox().apply {
        icon = Icons.Static.UncheckedCheckBox
        selectedIcon = Icons.Static.CheckedCheckBox
        disabledIcon = Icons.Static.DisabledCheckBox
        disabledSelectedIcon = Icons.Static.DisabledSelectedCheckBox
    }

    fun setOKButtonDefault() = setOKButtonIcon(Icons.Static.Send)
    fun setOKButtonLoading() = setOKButtonIcon(Icons.Animated.Dashes)
    fun setButtonDefault(button: JButton) = button.setIcon(Icons.Static.Send)
    fun setButtonLoading(button: JButton) = button.setIcon(Icons.Animated.Dashes)

    fun showMessageDialog(message: String, title: String, messageType: Int) =
        showMessageDialogRaw(get(message), get(title), messageType)

    fun showMessageDialog(message: String, title: String, messageType: Int, icon: Icon) =
        showMessageDialogRaw(get(message), get(title), messageType, icon)

    fun showSuccessDialog(message: String, title: String) =
        showSuccessDialogRaw(get(message), get(title))

    fun showFailedDialog(message: String, title: String) =
        showFailedDialogRaw(get(message), get(title))

    fun showMessageDialogRaw(message: String, title: String, messageType: Int, icon: Icon? = null) {
        JOptionPane.showMessageDialog(contentPanel, message, title, messageType, icon)
    }

    fun showSuccessDialogRaw(message: String, title: String) =
        showMessageDialogRaw(message, title, JOptionPane.OK_CANCEL_OPTION, Icons.Static.Success)

    fun showFailedDialogRaw(message: String, title: String) =
        showMessageDialogRaw(message, title, JOptionPane.ERROR_MESSAGE, Icons.Static.Failed)

    fun get(@PropertyKey(resourceBundle = Lang.FILE) key: String): String = Lang.get(key)
    fun get(@PropertyKey(resourceBundle = Lang.FILE) key: String, vararg params: Any): String = Lang.get(key, *params)

    protected fun FormBuilder.addPlatformSection(platformName: String, icon: Icon? = null, vararg fields: FieldConfig) {
        addComponent(createSectionLabel(platformName, icon))
        addComponent(SeparatorComponent(JBUI.scale(5)))
        for (field in fields) {
            if (field.label.isNullOrEmpty()) {
                addComponent(field.fieldBlock.invoke())
            } else {
                addLabeledComponent(createFieldLabel(field.label), field.fieldBlock.invoke())
            }
        }
        addVerticalGap(JBUI.scale(15))
    }
}