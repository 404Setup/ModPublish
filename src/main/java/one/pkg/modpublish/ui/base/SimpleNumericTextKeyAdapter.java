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

package one.pkg.modpublish.ui.base;

import com.intellij.ui.components.JBTextField;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SimpleNumericTextKeyAdapter extends KeyAdapter {
    private final JBTextField field;
    private final int minValue;

    public SimpleNumericTextKeyAdapter(JBTextField field, int minValue) {
        this.field = field;
        this.minValue = minValue;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != '-') {
            e.consume();
        }
        // Only allow minus sign at the beginning and if minValue is negative
        if (c == '-' && (field.getCaretPosition() != 0 || minValue >= 0)) {
            e.consume();
        }
    }
}
