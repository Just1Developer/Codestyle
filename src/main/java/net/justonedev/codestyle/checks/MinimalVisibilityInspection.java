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
                        new LowerVisibilityQuickFix(visibilityInfo.getSuggestedLevel())
                );
            }
        };
    }

    /**
     * The same logic you had, but now factoring in your config (allowProtectedIfOverrides, etc.).
     */
    private VisibilityInfo analyzeMethodUsage(PsiMethod method) {
        VisibilityInfo info = new VisibilityInfo(getVisibility(method));

        // 1. Query references in the entire project
        Query<PsiReference> search = ReferencesSearch.search(method, GlobalSearchScope.projectScope(method.getProject()));
        // 2. Examine references
        for (PsiReference ref : search) {
            PsiElement element = ref.getElement();
            PsiClass usageClass = findContainingClass(element);
            if (usageClass == null) {
                // unknown usage => must remain public
                info.setPublicUsageFound(true);
                break;
            } else {
                PsiClass methodClass = method.getContainingClass();
                if (methodClass != null && !inSameClass(methodClass, usageClass)) {
                    if (inSamePackage(methodClass, usageClass)) {
                        // we can only do package-private at best
                        info.setPackagePrivateUsageFound(true);
                    } else if (isSubclass(methodClass, usageClass)) {
                        // could do protected
                        info.setProtectedUsageFound(true);
                    } else {
                        // must be public
                        info.setPublicUsageFound(true);
                        break;
                    }
                }
            }
        }

        // 3. Possibly refine based on user config. For example:
        // if allowProtectedIfOverrides = false, then do not allow protected if method is overridden:
        if (!allowProtectedIfOverrides) {
            // If this method has overrides, then protected might not be considered valid
            boolean hasOverrides = OverridingMethodsSearch.search(method).findFirst() != null;
            if (hasOverrides) {
                // Force 'public' if it’s overridden, ignoring the normal logic
                info.setPublicUsageFound(true);
            }
        }

        // If user does not allow package-private, we treat that as needing at least protected
        if (!allowPackagePrivate && !info.isPublicUsageFound() && !info.isProtectedUsageFound()) {
            // So if we are currently at "package-private" or "private" possibility,
            // we push it up to "protected"
            info.setProtectedUsageFound(true);
        }

        return info;
    }

    // Helper methods, same as you have now
    private PsiClass findContainingClass(PsiElement element) { /*...*/ return null; }
    private boolean inSameClass(PsiClass c1, PsiClass c2) { /*...*/ return false; }
    private boolean inSamePackage(PsiClass c1, PsiClass c2) { /*...*/ return false; }
    private boolean isSubclass(PsiClass base, PsiClass maybeSubclass) { /*...*/ return false; }

    private String getVisibility(PsiMethod method) { /*...*/ return PsiModifier.PUBLIC; }

    /**
     * Optional: a QuickFix to automatically reduce the method visibility.
     * The user can click "Apply fix" in the inspection results.
     */
    private static class LowerVisibilityQuickFix implements LocalQuickFix {
        private final String newVisibility;

        public LowerVisibilityQuickFix(String newVisibility) {
            this.newVisibility = newVisibility;
        }

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
            if (!(element.getParent() instanceof PsiMethod)) return;
            PsiMethod method = (PsiMethod) element.getParent();

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
