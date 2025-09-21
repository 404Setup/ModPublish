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

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record FieldConfig(String label, Supplier<JComponent> fieldSupplier) {
    public FieldConfig(Supplier<JComponent> fieldSupplier) {
        this(null, fieldSupplier);
    }

    public static FieldConfig of(String label, Supplier<JComponent> fieldSupplier) {
        return new FieldConfig(label, fieldSupplier);
    }

    public static FieldConfig of(Supplier<JComponent> fieldSupplier) {
        return new FieldConfig(fieldSupplier);
    }

    public static FieldConfig of(Consumer<JButton> buttonConsumer) {
        return new FieldConfig(() -> {
            JButton button = new JButton();
            buttonConsumer.accept(button);
            return button;
        });
    }
}
