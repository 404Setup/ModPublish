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

package one.pkg.modpublish.ui.panel;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.ui.AddDependencyDialog;
import one.pkg.modpublish.ui.PublishModDialog;
import one.pkg.modpublish.util.resources.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class DependencyManagerPanel extends JPanel {
    private final PublishModDialog parentDialog;
    private final List<DependencyInfo> dependencies;
    private final JPanel dependencyListPanel;

    public DependencyManagerPanel(PublishModDialog parentDialog) {
        super(new BorderLayout());
        this.parentDialog = parentDialog;
        this.dependencies = new ArrayList<>();

        JBLabel titleLabel = new JBLabel(Lang.get("component.name.depend-manager"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));

        JButton addButton = new JButton(Lang.get("title.add-dependency"));
        addButton.addActionListener(this::onAddDependency);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);

        dependencyListPanel = new JPanel();
        dependencyListPanel.setLayout(new BoxLayout(dependencyListPanel, BoxLayout.Y_AXIS));

        JBScrollPane scrollPane = new JBScrollPane(dependencyListPanel);
        scrollPane.setPreferredSize(new Dimension(600, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void onAddDependency(ActionEvent e) {
        boolean[] publishTargets = parentDialog.getPublishTargets();

        AddDependencyDialog dialog = new AddDependencyDialog(parentDialog, publishTargets);
        if (dialog.showAndGet() && dialog.isOK()) {
            DependencyInfo dependency = dialog.getDependency();
            addDependency(dependency);
        }
    }

    public void addDependency(DependencyInfo dependency) {
        dependencies.add(dependency);
        refreshDependencyList();
    }

    public void removeDependency(DependencyInfo dependency) {
        dependencies.remove(dependency);
        refreshDependencyList();
    }

    private void refreshDependencyList() {
        dependencyListPanel.removeAll();

        for (DependencyInfo dependency : dependencies) {
            JPanel depPanel = createDependencyPanel(dependency);
            dependencyListPanel.add(depPanel);
        }

        dependencyListPanel.revalidate();
        dependencyListPanel.repaint();
    }

    private JPanel createDependencyPanel(DependencyInfo dependency) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        String displayText = String.format("%s (%s) - %s",
                dependency.getCustomTitle() != null && !dependency.getCustomTitle().isBlank()
                        ? dependency.getCustomTitle() : "Unknown Dependency",
                dependency.getProjectId(),
                dependency.getType().getDisplayName());

        JBLabel depLabel = new JBLabel(displayText);

        JButton removeButton = new JButton(Lang.get("button.delete"));
        removeButton.addActionListener(e -> removeDependency(dependency));

        panel.add(depLabel, BorderLayout.CENTER);
        panel.add(removeButton, BorderLayout.EAST);

        return panel;
    }

    public List<DependencyInfo> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    public void setDependencies(List<DependencyInfo> dependencies) {
        this.dependencies.clear();
        if (dependencies != null) this.dependencies.addAll(dependencies);
        refreshDependencyList();
    }
}