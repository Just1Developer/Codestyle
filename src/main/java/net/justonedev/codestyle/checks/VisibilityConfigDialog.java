package net.justonedev.codestyle.checks;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class VisibilityConfigDialog extends DialogWrapper {

    private JComboBox<String> protectedCombo;
    private JComboBox<String> packagePrivateCombo;

    public VisibilityConfigDialog() {
        super(true); // can be modal
        init();
        setTitle("Configure Visibility Inspection");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

        // protected
        panel.add(new JLabel("protected:"));
        protectedCombo = new JComboBox<>(new String[]{
                "When applicable",
                "With Inheritors",
                "Never",
                "Always"
        });
        panel.add(protectedCombo);

        // package-private
        panel.add(new JLabel("package-private:"));
        packagePrivateCombo = new JComboBox<>(new String[]{"Use", "Never"});
        panel.add(packagePrivateCombo);

        return panel;
    }

    public String getProtectedSelection() {
        return (String) protectedCombo.getSelectedItem();
    }

    public String getPackagePrivateSelection() {
        return (String) packagePrivateCombo.getSelectedItem();
    }
}
