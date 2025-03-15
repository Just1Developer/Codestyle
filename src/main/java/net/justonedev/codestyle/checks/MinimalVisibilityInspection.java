package net.justonedev.codestyle.checks;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class MinimalVisibilityInspection extends LocalInspectionTool {
    /**
     * These fields store the user’s configuration. For example:
     */
    public boolean allowProtectedIfOverrides = true; // “With Inheritors”
    public boolean allowPackagePrivate = false;      // “Never (package-private)”

    /**
     * The shortName must match the 'shortName' in plugin.xml if you use localInspection extension points.
     */
    @Override
    public @NotNull String getShortName() {
        return "MinimalVisibilityInspection";
    }

    @Override
    public @Nullable JComponent createOptionsPanel() {
        // This builds a small panel with two checkboxes or drop-downs, for demonstration.

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Example 1: allowProtectedIfOverrides
        JBCheckBox checkProtected = new JBCheckBox("Allow 'protected' if method has overrides", allowProtectedIfOverrides);
        checkProtected.addActionListener(e -> allowProtectedIfOverrides = checkProtected.isSelected());
        panel.add(checkProtected, gbc);

        // Example 2: allowPackagePrivate
        gbc.gridy++;
        JBCheckBox checkPackagePrivate = new JBCheckBox("Allow 'package-private'", allowPackagePrivate);
        checkPackagePrivate.addActionListener(e -> allowPackagePrivate = checkPackagePrivate.isSelected());
        panel.add(checkPackagePrivate, gbc);

        gbc.gridy++;
        panel.add(new JBLabel("<html><i>Adjust how minimal visibility is assigned</i></html>"), gbc);

        return panel;
    }

    /**
     * The main entry point: for a Java inspection, we typically override buildVisitor().
     * We return a PsiElementVisitor that visits methods (PsiMethod) in a Java file
     * and checks if each method can be lowered in visibility.
     */
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                                   boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethod(@NotNull PsiMethod method) {
                super.visitMethod(method);

                VisibilityInfo visibilityInfo = MinimalVisibilityCheck.analyzeMethodUsage(method, holder.getProject());
                if (!visibilityInfo.canLowerVisibility()) {
                    return; // no problem
                }

                // 3. If canLowerVisibility, register a Problem
                String message = "Method visibility can be lowered to '" + visibilityInfo.getSuggestedLevel() + "'";
                holder.registerProblem(
                        Objects.requireNonNull(method.getNameIdentifier()),  // highlight the method name
                        message,
                        ProblemHighlightType.WEAK_WARNING,
                        new LowerVisibilityQuickFix(visibilityInfo.getSuggestedLevel().getVisibility())
                );
            }
        };
    }

    /**
     * Optional: a QuickFix to automatically reduce the method visibility.
     * The user can click "Apply fix" in the inspection results.
     */
    private record LowerVisibilityQuickFix(String newVisibility) implements LocalQuickFix {

        @Override
        public @NotNull String getName() {
            return "Change visibility to '" + newVisibility + "'";
        }

        @Override
        public @NotNull String getFamilyName() {
            return "Lower visibility quick fix";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            if (!(element.getParent() instanceof PsiMethod method)) return;

            PsiModifierList modifiers = method.getModifierList();
            // First remove existing visibility modifiers
            modifiers.setModifierProperty(PsiModifier.PUBLIC, false);
            modifiers.setModifierProperty(PsiModifier.PROTECTED, false);
            modifiers.setModifierProperty(PsiModifier.PRIVATE, false);

            // Then set the new one
            if (PsiModifier.PUBLIC.equals(newVisibility)) {
                modifiers.setModifierProperty(PsiModifier.PUBLIC, true);
            } else if (PsiModifier.PROTECTED.equals(newVisibility)) {
                modifiers.setModifierProperty(PsiModifier.PROTECTED, true);
            } else if ("package-private".equals(newVisibility)) {
                // do nothing, i.e. no explicit keyword
            } else if (PsiModifier.PRIVATE.equals(newVisibility)) {
                modifiers.setModifierProperty(PsiModifier.PRIVATE, true);
            }
        }
    }
}
