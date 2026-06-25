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

package one.pkg.modpublish.ui.renderer

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

class LFRenderer: DocumentListener {
    private var isNormalizing = false

    override fun documentChanged(event: DocumentEvent) {
        if (isNormalizing) return
        val doc = event.document
        val raw = doc.text
        val normalized = raw.replace("\r\n", "\n").replace("\r", "\n")
        if (normalized != raw) {
            isNormalizing = true
            try {
                WriteCommandAction.runWriteCommandAction(null) {
                    doc.setText(normalized)
                }
            } finally {
                isNormalizing = false
            }
        }
    }
}