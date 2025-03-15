package net.justonedev.codestyle.checks;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MinimalVisibilityToolWindowPanel {
    private final JPanel mainPanel;
    private final VisibilityTableModel tableModel;

    public MinimalVisibilityToolWindowPanel(Project project) {
        mainPanel = new JPanel(new BorderLayout());

        // Top panel with the "Inspect Code..." button
        JPanel topPanel = new JPanel();
        JButton inspectButton = new JButton("Inspect Code...");
        topPanel.add(inspectButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Table to show results
        tableModel = new VisibilityTableModel();
        JBTable resultsTable = new JBTable(tableModel);
        mainPanel.add(new JBScrollPane(resultsTable), BorderLayout.CENTER);

        inspectButton.addActionListener(e -> {
            // 1. Show your config dialog
            VisibilityConfigDialog dialog = new VisibilityConfigDialog();
            if (!dialog.showAndGet()) {
                // User canceled
                return;
            }

            // Retrieve user selections
            String protectedSetting = dialog.getProtectedSelection();
            String packagePrivateSetting = dialog.getPackagePrivateSelection();

            VisibilitySettings settings = new VisibilitySettings(protectedSetting, packagePrivateSetting);

            // 2. Run the scanning logic in background
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Minimal visibility scan") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    // Optionally set some text for the progress bar
                    indicator.setText("Scanning project for minimal visibility issues...");

                    // If your runScan method uses PSI, consider wrapping it in a read action:
                    List<VisibilityResult> results = ApplicationManager.getApplication().runReadAction(
                            (Computable<List<VisibilityResult>>) () -> {
                                // Actual scanning logic
                                return MinimalVisibilityScanHelper.runScan(project, settings);
                            }
                    );

                    // 3. Update the table model on the EDT
                    ApplicationManager.getApplication().invokeLater(() -> {
                        tableModel.setResults(results);
                    });
                }
            });
        });


        // Add a mouse listener to handle double-click navigation
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check for double-click and left mouse button
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = resultsTable.rowAtPoint(e.getPoint());
                    // Ensure the user didn't click outside the table rows
                    if (row >= 0) {
                        // Convert the row to the model index if there's sorting
                        int modelRow = resultsTable.convertRowIndexToModel(row);

                        VisibilityResult result = tableModel.getResultAt(modelRow);
                        if (result != null && result.getFile() != null) {
                            // Example: navigate to file offset, or line/column
                            // if your result class tracks a line number, convert to offset, etc.

                            // Suppose result has getOffset() – a text offset in the file
                            new OpenFileDescriptor(
                                    project,
                                    result.getFile().getVirtualFile(),
                                    result.getOffset()  // or line/column if you prefer
                            ).navigate(true);
                        }
                    }
                }
            }
        });
    }

    public JComponent getMainComponent() {
        return mainPanel;
    }
}
