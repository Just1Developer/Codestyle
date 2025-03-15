package net.justonedev.codestyle.checks;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MinimalVisibilityScanHelper {

    public static List<VisibilityResult> runScan(Project project,
                                                 VisibilitySettings settings) {
        // 1. Collect all classes
        Collection<PsiClass> allClasses = AllClassesSearch
                .search(GlobalSearchScope.projectScope(project), project)
                .findAll();

        List<VisibilityResult> results = new ArrayList<>();

        // 2. For each class, check methods
        for (PsiClass psiClass : allClasses) {
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                Visibility currentVisibility = Visibility.fromMethod(psiMethod);
                if (currentVisibility.getLevel() == 0) continue;
                VisibilityInfo info = MinimalVisibilityCheck.analyzeMethodUsage(psiMethod, project);

                if (info.canLowerVisibility(settings)) {
                    results.add(new VisibilityResult(
                            psiClass,
                            psiMethod,
                            currentVisibility,
                            info.getSuggestedLevel(settings)
                    ));
                }
            }
        }

        return results;
    }
}
