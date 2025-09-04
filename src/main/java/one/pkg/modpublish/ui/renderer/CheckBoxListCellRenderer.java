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

package one.pkg.modpublish.ui.renderer;

import one.pkg.modpublish.data.internel.MinecraftVersionItem;

import javax.swing.*;
import java.awt.*;

public class CheckBoxListCellRenderer extends JCheckBox implements ListCellRenderer<MinecraftVersionItem> {
    public CheckBoxListCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends MinecraftVersionItem> list,
            MinecraftVersionItem value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        setSelected(value.isSelected());
        setText(value.getVersion().version);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}
