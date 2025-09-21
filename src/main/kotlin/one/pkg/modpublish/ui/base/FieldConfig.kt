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

import java.util.function.Consumer
import java.util.function.Supplier
import javax.swing.JButton
import javax.swing.JComponent

@JvmRecord
data class FieldConfig(@JvmField val label: String?, @JvmField val fieldBlock: () -> JComponent) {
    constructor(fieldBlock: () -> JComponent) : this(null, fieldBlock)

    companion object {
        @JvmStatic
        fun of(label: String?, fieldBlock: () -> JComponent): FieldConfig {
            return FieldConfig(label, fieldBlock)
        }

        @JvmStatic
        fun of(fieldBlock: () -> JComponent): FieldConfig {
            return FieldConfig(fieldBlock)
        }

        @JvmStatic
        fun btn(buttonConsumer: Consumer<JButton>): FieldConfig {
            return FieldConfig {
                val button = JButton()
                buttonConsumer.accept(button)
                button
            }
        }
    }
}
