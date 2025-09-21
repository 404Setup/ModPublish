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

import javax.swing.text.AttributeSet
import javax.swing.text.BadLocationException
import javax.swing.text.DocumentFilter

class UniversalNumericDocumentFilter(
    var minValue: Double = Double.NEGATIVE_INFINITY,
    var maxValue: Double = Double.POSITIVE_INFINITY,
    private val allowDecimal: Boolean = true,
    private val decimalPlaces: Int = 2
) : DocumentFilter() {

    @Throws(BadLocationException::class)
    override fun insertString(fb: FilterBypass, offset: Int, string: String?, attr: AttributeSet?) {
        string ?: return
        val newText = buildNewText(fb, offset, 0, string)
        if (isValidNumber(newText)) {
            super.insertString(fb, offset, string, attr)
            adjustValueIfNeeded(fb)
        }
    }

    @Throws(BadLocationException::class)
    override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String?, attrs: AttributeSet?) {
        text ?: return
        val newText = buildNewText(fb, offset, length, text)
        if (isValidNumber(newText)) {
            super.replace(fb, offset, length, text, attrs)
            adjustValueIfNeeded(fb)
        }
    }

    private fun buildNewText(fb: FilterBypass, offset: Int, length: Int, text: String): String {
        val current = fb.document.getText(0, fb.document.length)
        return current.take(offset) + text + current.drop(offset + length)
    }

    private fun isValidNumber(text: String): Boolean {
        if (text.isEmpty() || text == "-" || text == "." || text == "-.") return true

        return if (allowDecimal) {
            text.toDoubleOrNull() ?: return false
            val parts = text.split(".")
            if (parts.size == 2 && parts[1].length > decimalPlaces) return false
            true
        } else {
            text.toLongOrNull() != null
        }
    }

    @Throws(BadLocationException::class)
    private fun adjustValueIfNeeded(fb: FilterBypass) {
        val text = fb.document.getText(0, fb.document.length)
        if (text.isEmpty() || text == "-" || text == "." || text == "-.") return

        val value = if (allowDecimal) text.toDoubleOrNull() else text.toLongOrNull()?.toDouble()
        value ?: return

        val newValue = value.coerceIn(minValue, maxValue).let {
            if (!allowDecimal) it.toLong().toString() else it.toString()
        }

        if (newValue != text) {
            fb.replace(0, fb.document.length, newValue, null)
        }
    }
}
