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
        setText(value.getVersion().v);

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
