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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class SimpleNumericTextDocumentFilter extends DocumentFilter {
    private final int minValue;
    private final int maxValue;

    public SimpleNumericTextDocumentFilter(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) return;
        String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
        if (isValidNumber(newText)) {
            super.insertString(fb, offset, string, attr);
            adjustValueIfNeeded(fb);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) return;
        String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
        String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
        if (isValidNumber(newText)) {
            super.replace(fb, offset, length, text, attrs);
            adjustValueIfNeeded(fb);
        }
    }

    private boolean isValidNumber(String text) {
        if (text.isEmpty() || text.equals("-")) return true;
        try {
            Long.parseLong(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void adjustValueIfNeeded(FilterBypass fb) throws BadLocationException {
        String text = fb.getDocument().getText(0, fb.getDocument().getLength());
        if (text.isEmpty() || text.equals("-")) return;

        try {
            long value = Long.parseLong(text);
            String newText;
            if (value < minValue) {
                newText = String.valueOf(minValue);
            } else if (value > maxValue) {
                newText = String.valueOf(maxValue);
            } else {
                return;
            }

            fb.replace(0, fb.getDocument().getLength(), newText, null);
        } catch (NumberFormatException ignored) {
        }
    }
}
