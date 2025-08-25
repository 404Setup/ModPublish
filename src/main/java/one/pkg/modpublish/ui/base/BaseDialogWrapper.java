package one.pkg.modpublish.ui.base;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import one.pkg.modpublish.resources.Lang;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public abstract class BaseDialogWrapper extends DialogWrapper {
    final Project project;

    public BaseDialogWrapper(Project project) {
        super(project);
        this.project = project;
    }

    public BaseDialogWrapper(Project project, boolean canBeParent) {
        super(project, canBeParent);
        this.project = project;
    }

    public JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtil.getFont(UIUtil.FontSize.NORMAL, null));
        return label;
    }

    public JBTextField createTextField() {
        JBTextField field = new JBTextField();
        field.setPreferredSize(new Dimension(250, field.getPreferredSize().height));
        return field;
    }

    public JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        Font currentFont = UIUtil.getFont(UIUtil.FontSize.NORMAL, null);
        label.setFont(currentFont.deriveFont(Font.BOLD));
        label.setForeground(UIUtil.getLabelForeground());
        return label;
    }

    public JLabel createSectionLabel(String text, Icon icon) {
        JLabel label = createSectionLabel(text);
        if (icon != null)
            label.setIcon(icon);
        return label;
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
        if (jComponent.isEnabled())
            jComponent.setToolTipText(get(key));
    }

    public void setToolTipText(@PropertyKey(resourceBundle = Lang.File) String key, JComponent jComponent, Object... params) {
        if (jComponent.isEnabled())
            jComponent.setToolTipText(get(key, params));
    }

    public JBLabel getJBLabel(@PropertyKey(resourceBundle = Lang.File) String key) {
        return new JBLabel(get(key));
    }

    public JBCheckBox getJBCheckBox(@PropertyKey(resourceBundle = Lang.File) String key) {
        return new JBCheckBox(get(key));
    }

    @SuppressWarnings("all")
    public void showMessageDialog(@PropertyKey(resourceBundle = Lang.File) String message,
                                  @PropertyKey(resourceBundle = Lang.File) String title,
                                  int messageType
    ) throws HeadlessException {
        showMessageDialogRaw(get(message), get(title), messageType);
    }

    @SuppressWarnings("all")
    public void showMessageDialogRaw(String message, String title, int messageType
    ) throws HeadlessException {
        JOptionPane.showMessageDialog(getContentPanel(), message, title, messageType);
    }

    public String get(@PropertyKey(resourceBundle = Lang.File) String key) {
        return Lang.get(key);
    }

    public String get(@PropertyKey(resourceBundle = Lang.File) String key, Object... params) {
        return Lang.get(key, params);
    }

    protected void addPlatformSection(FormBuilder formBuilder, String platformName,
                                      @Nullable String iconPath, FieldConfig... fields) {
        formBuilder.addComponent(createSectionLabel(platformName,
                iconPath == null ? null : IconLoader.getIcon(iconPath, getClass())
        ));
        formBuilder.addComponent(new SeparatorComponent(JBUI.scale(5)));

        for (FieldConfig field : fields)
            formBuilder.addLabeledComponent(createFieldLabel(field.label), field.fieldSupplier.get());

        formBuilder.addVerticalGap(JBUI.scale(15));
    }

    public void setDisabledWithTooltip(JBTextField field) {
        field.setEnabled(false);
        field.setToolTipText(get("tooltip.gitlab.disable"));
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
                dialogWrapper.setTitle(Lang.get(key));
            }
        };

        abstract void setText(@PropertyKey(resourceBundle = Lang.File) String key, BaseDialogWrapper dialogWrapper);
    }

    public record FieldConfig(String label, Supplier<JBTextField> fieldSupplier) {
    }
}
