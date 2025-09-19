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

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import one.pkg.modpublish.ui.icon.Icons;
import one.pkg.modpublish.util.resources.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public abstract class BaseDialogWrapper extends DialogWrapper {
    final Project project;

    public BaseDialogWrapper(Project project) {
        super(project);
        this.project = project;
    }

    public BaseDialogWrapper(@NotNull Project project, boolean canBeParent) {
        super(project, canBeParent);
        this.project = project;
    }

    public JComponent toScrollPanel(FormBuilder formBuilder, int width, int height) {
        JPanel panel = formBuilder.getPanel();
        panel.setBorder(JBUI.Borders.empty(20, 20, 15, 20));

        JBScrollPane scrollPane = new JBScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(width, height));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    public @NotNull JLabel createFieldLabel(@NotNull String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtil.getFont(UIUtil.FontSize.NORMAL, null));
        return label;
    }

    public @NotNull JBTextField createTextField() {
        JBTextField field = new JBTextField();
        field.setPreferredSize(new Dimension(250, field.getPreferredSize().height));
        return field;
    }

    private JBTextField createNumericTextField() {
        JBTextField field = new JBTextField();
        field.setPreferredSize(new Dimension(250, field.getPreferredSize().height));

        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null && string.matches("\\d*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        return field;
    }

    public @NotNull JBTextField createSimpleNumericTextField(int minValue, int maxValue) {
        if (minValue >= maxValue)
            throw new IllegalArgumentException("minValue must be greater than maxValue");
        JBTextField field = new JBTextField();
        field.setPreferredSize(new Dimension(250, field.getPreferredSize().height));

        field.addKeyListener(new KeyAdapter() {
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
        });

        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
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
        });

        return field;
    }

    public @NotNull JBTextField createSimpleNumericTextField() {
        return createSimpleNumericTextField(0, Integer.MAX_VALUE);
    }

    public @NotNull JBLabel createLabel(@NotNull String text) {
        JBLabel label = new JBLabel("<html><small>" + text + "</small></html>");
        label.setForeground(JBColor.GRAY);
        return label;
    }


    public @NotNull ActionLink createActionLink(@NotNull String text, @NotNull String url) {
        ActionLink externalLink = new ActionLink(text);
        externalLink.addActionListener(event -> BrowserUtil.browse(url));
        externalLink.setExternalLinkIcon();
        return externalLink;
    }

    public @NotNull JLabel createSectionLabel(@NotNull String text) {
        JLabel label = new JLabel(text);
        Font currentFont = UIUtil.getFont(UIUtil.FontSize.NORMAL, null);
        label.setFont(currentFont.deriveFont(Font.BOLD));
        label.setForeground(UIUtil.getLabelForeground());
        return label;
    }

    public @NotNull JLabel createSectionLabel(@NotNull String text, @Nullable Icon icon) {
        JLabel label = createSectionLabel(text);
        if (icon != null) label.setIcon(icon);
        return label;
    }

    // This is the only way, slightly ugly but better than direct setting or no setting at all
    public void setErrorStyle(@NotNull JBCheckBox checkBox) {
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(new JBColor(new Color(255, 200, 200), new Color(255, 200, 200)));
        errorPanel.setBorder(BorderFactory.createLineBorder(JBColor.RED, 2));

        Container parent = checkBox.getParent();
        if (parent != null) {
            Component[] components = parent.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == checkBox) {
                    parent.remove(checkBox);
                    errorPanel.add(checkBox, BorderLayout.CENTER);
                    parent.add(errorPanel, i);
                    parent.revalidate();
                    parent.repaint();
                    break;
                }
            }
        }
    }

    @Override
    public void setTitle(@PropertyKey(resourceBundle = Lang.File) String key) {
        super.setTitle(get(key));
    }

    public void setTitle(@PropertyKey(resourceBundle = Lang.File) String key, Object... params) {
        super.setTitle(get(key, params));
    }

    public void setText(@PropertyKey(resourceBundle = Lang.File) String key, TextType type) {
        type.setText(key, this);
    }

    public void setToolTipText(@PropertyKey(resourceBundle = Lang.File) String key, JComponent jComponent) {
        jComponent.setToolTipText(get(key));
    }

    public void setToolTipText(@PropertyKey(resourceBundle = Lang.File) String key, JComponent jComponent, Object... params) {
        jComponent.setToolTipText(get(key, params));
    }

    public JBLabel getJBLabel(@PropertyKey(resourceBundle = Lang.File) String key) {
        return new JBLabel(get(key));
    }

    public JBCheckBox getJBCheckBox(@PropertyKey(resourceBundle = Lang.File) String key) {
        return getJBCheckBoxRaw(get(key));
    }

    public JBCheckBox getJBCheckBoxRaw(String key) {
        var box = getJBCheckBox();
        box.setText(key);
        return box;
    }

    public JBCheckBox getJBCheckBox() {
        var box = new JBCheckBox();
        box.setIcon(Icons.Static.UncheckedCheckBox);
        box.setSelectedIcon(Icons.Static.CheckedCheckBox);
        box.setDisabledIcon(Icons.Static.DisabledCheckBox);
        box.setDisabledSelectedIcon(Icons.Static.DisabledSelectedCheckBox);
        return box;
    }

    public void setOKButtonDefault() {
        setOKButtonIcon(Icons.Static.Send);
    }

    public void setOKButtonLoading() {
        setOKButtonIcon(Icons.Animated.Dashes);
    }

    public void setButtonDefault(JButton button) {
        button.setIcon(Icons.Static.Send);
    }

    public void setButtonLoading(JButton button) {
        button.setIcon(Icons.Animated.Dashes);
    }

    public void setButtonNull(JButton button) {
        button.setIcon(null);
    }

    @SuppressWarnings("all")
    public void showMessageDialog(@PropertyKey(resourceBundle = Lang.File) String message,
                                  @PropertyKey(resourceBundle = Lang.File) String title,
                                  int messageType
    ) throws HeadlessException {
        showMessageDialogRaw(get(message), get(title), messageType);
    }

    @SuppressWarnings("all")
    public void showMessageDialog(@PropertyKey(resourceBundle = Lang.File) String message,
                                  @PropertyKey(resourceBundle = Lang.File) String title,
                                  int messageType,
                                  Icon icon
    ) throws HeadlessException {
        showMessageDialogRaw(get(message), get(title), messageType, icon);
    }

    public void showSuccessDialog(@PropertyKey(resourceBundle = Lang.File) String message, @PropertyKey(resourceBundle = Lang.File) String title) {
        showSuccessDialogRaw(get(message), get(title));
    }

    public void showFailedDialog(@PropertyKey(resourceBundle = Lang.File) String message, @PropertyKey(resourceBundle = Lang.File) String title) {
        showFailedDialogRaw(get(message), get(title));
    }

    @SuppressWarnings("all")
    public void showMessageDialogRaw(@NotNull String message, @NotNull String title, int messageType
    ) throws HeadlessException {
        JOptionPane.showMessageDialog(getContentPanel(), message, title, messageType);
    }

    @SuppressWarnings("all")
    public void showMessageDialogRaw(@NotNull String message, @NotNull String title, int messageType, Icon icon
    ) throws HeadlessException {
        JOptionPane.showMessageDialog(getContentPanel(), message, title, messageType, icon);
    }

    public void showSuccessDialogRaw(@NotNull String message, @NotNull String title) {
        showMessageDialogRaw(message, title, JOptionPane.OK_CANCEL_OPTION, Icons.Static.Success);
    }

    public void showFailedDialogRaw(@NotNull String message, @NotNull String title) {
        showMessageDialogRaw(message, title, JOptionPane.ERROR_MESSAGE, Icons.Static.Failed);
    }

    public String get(@PropertyKey(resourceBundle = Lang.File) String key) {
        return Lang.get(key);
    }

    public String get(@PropertyKey(resourceBundle = Lang.File) String key, Object... params) {
        return Lang.get(key, params);
    }

    protected void addPlatformSection(@NotNull FormBuilder formBuilder, @NotNull String platformName,
                                      @Nullable Icon icon, FieldConfig... fields) {
        formBuilder.addComponent(createSectionLabel(platformName, icon));
        formBuilder.addComponent(new SeparatorComponent(JBUI.scale(5)));

        for (FieldConfig field : fields) {
            if (field.label == null || field.label.isEmpty())
                formBuilder.addComponent(field.fieldSupplier.get());
            else formBuilder.addLabeledComponent(createFieldLabel(field.label), field.fieldSupplier.get());
        }
        formBuilder.addVerticalGap(JBUI.scale(15));
    }

    public enum TextType {
        OKButton() {
            @Override
            void setText(@PropertyKey(resourceBundle = Lang.File) String key, BaseDialogWrapper dialogWrapper) {
                dialogWrapper.setOKButtonText(Lang.get(key));
            }
        },
        CancelButton() {
            @Override
            void setText(@PropertyKey(resourceBundle = Lang.File) String key, BaseDialogWrapper dialogWrapper) {
                dialogWrapper.setCancelButtonText(Lang.get(key));
            }
        },
        Title() {
            @Override
            void setText(@PropertyKey(resourceBundle = Lang.File) String key, BaseDialogWrapper dialogWrapper) {
                dialogWrapper.setTitle(key);
            }
        };

        abstract void setText(@PropertyKey(resourceBundle = Lang.File) String key, BaseDialogWrapper dialogWrapper);
    }

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
    }
}
