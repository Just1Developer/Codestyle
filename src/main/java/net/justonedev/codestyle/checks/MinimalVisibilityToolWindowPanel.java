package net.justonedev.codestyle.checks;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MinimalVisibilityToolWindowPanel {
    private final Project project;
    private final JPanel mainPanel;
    private final JButton inspectButton;
    private final JBTable resultsTable;
    private final VisibilityTableModel tableModel;

    public MinimalVisibilityToolWindowPanel(Project project) {
        this.project = project;

        mainPanel = new JPanel(new BorderLayout());

        // Top panel with the “Inspect Code…” button
        JPanel topPanel = new JPanel();
        inspectButton = new JButton("Inspect Code...");
        topPanel.add(inspectButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Table to show results
        tableModel = new VisibilityTableModel();
        resultsTable = new JBTable(tableModel);
        mainPanel.add(new JBScrollPane(resultsTable), BorderLayout.CENTER);

        // Add a listener to “Inspect Code...” that opens the config dialog, then scans
        inspectButton.addActionListener(e -> {
            // 1. Show your config dialog
            VisibilityConfigDialog dialog = new VisibilityConfigDialog();
            if (dialog.showAndGet()) {
                // The user clicked OK. Retrieve user selections:
                String protectedSetting = dialog.getProtectedSelection();
                String packagePrivateSetting = dialog.getPackagePrivateSelection();

                // 2. Perform the scanning logic (like your “actionPerformed” method).
                List<VisibilityResult> results = MinimalVisibilityScanHelper.runScan(
                        project, protectedSetting, packagePrivateSetting
                );

                // 3. Update the table model
                tableModel.setResults(results);
            }
        });
    }

    public JComponent getMainComponent() {
        return mainPanel;
    }
}
