package net.justonedev.codestyle.checks;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MinimalVisibilityScanHelper {

    public static List<VisibilityResult> runScan(Project project,
                                                 String protectedSetting,
                                                 String packagePrivateSetting) {
        // 1. Collect all classes
        Collection<PsiClass> allClasses = AllClassesSearch
                .search(GlobalSearchScope.projectScope(project), project)
                .findAll();

        List<VisibilityResult> results = new ArrayList<>();

        // 2. For each class, check methods
        for (PsiClass psiClass : allClasses) {
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                // Maybe skip @Override or synthetic
                if (psiMethod.hasAnnotation("java.lang.Override") || !psiMethod.isPhysical()) {
                    continue;
                }

                // 3. Analyze usage
                String currentVisibility = MinimalVisibilityCheck.getVisibility(psiMethod);
                VisibilityInfo info = MinimalVisibilityCheck.analyzeMethodUsage(psiMethod, project);

                // You can incorporate the user’s settings from the combos
                // e.g. if (protectedSetting.equals("Never")) => do something

                if (info.canLowerVisibility()) {
                    results.add(new VisibilityResult(
                            psiClass.getQualifiedName(),
                            psiMethod.getName(),
                            currentVisibility,
                            info.getSuggestedLevel()
                    ));
                }
            }
        }

        return results;
    }
}
