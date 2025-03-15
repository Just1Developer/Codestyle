package net.justonedev.codestyle.checks;

import com.intellij.lang.jvm.JvmClassKind;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.MethodSignature;
import com.intellij.util.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class MinimalVisibilityCheck extends AnAction {

    /**
     * Performs the action logic.
     * <p>
     * It is called on the UI thread with all data in the provided {@link DataContext} instance.
     *
     * @param e Action Event
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        //Messages.showInfoMessage("Minimal 1 v2", "Title");

        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 1. Gather all PSI classes in the project.
        Collection<PsiClass> allClasses = AllClassesSearch.search(GlobalSearchScope.projectScope(project), project).findAll();

        // 2. For each class, iterate over methods.
        StringBuilder resultBuilder = new StringBuilder();
        for (PsiClass psiClass : allClasses) {
            for (PsiMethod psiMethod : psiClass.getMethods()) {

                // Exclude certain methods here
                if (psiMethod.hasAnnotation("java.lang.Override")
                    || !psiMethod.isPhysical()
                    || isMainMethod(psiMethod)
                    || OverridingMethodsSearch.search(psiMethod).findFirst() != null) continue;
                //if ()

                // 3. Determine if method is candidate for lowered visibility.
                VisibilityInfo visibilityInfo = analyzeMethodUsage(psiMethod, project);
                if (visibilityInfo.canLowerVisibility()) {
                    resultBuilder.append("Method: ")
                            .append(psiClass.getQualifiedName())
                            .append(".")
                            .append(psiMethod.getName())
                            .append(" => can be lowered to: ")
                            .append(visibilityInfo.getSuggestedLevel())
                            .append("\n");
                }
            }
        }

        if (!resultBuilder.isEmpty()) {
            Messages.showInfoMessage(project, resultBuilder.toString(), "Lower Visibility Suggestions");
        } else {
            Messages.showInfoMessage(project, "No methods found that can be lowered.", "Lower Visibility");
        }
    }

    private static boolean isMainMethod(PsiMethod method) {
        MethodSignature sign = method.getSignature(PsiSubstitutor.EMPTY);
        return sign.getName().equals("main")
                && sign.getParameterTypes().length == 1
                && Arrays.stream(sign.getParameterTypes()).allMatch(s -> s.getPresentableText().equals("String[]"));
    }

    /**
     * Analyzes a single PsiMethod’s references across the project to see if the method’s visibility can be lowered.
     */
    @SuppressWarnings("ApiStatus.Experimental")
    public static VisibilityInfo analyzeMethodUsage(PsiMethod method, Project project) {
        // Current visibility
        VisibilityInfo info = new VisibilityInfo(getVisibility(method));

        if (method.hasAnnotation("java.lang.Override")
                || !method.isPhysical()
                || isMainMethod(method)
                || Objects.requireNonNull(method.getContainingClass()).getClassKind().equals(JvmClassKind.INTERFACE)
                || OverridingMethodsSearch.search(method).findFirst() != null) return info;

        // 1. Query references in the entire project
        Query<PsiReference> search = ReferencesSearch.search(method, GlobalSearchScope.projectScope(project));
        // 2. Examine where references come from
        for (PsiReference ref : search) {
            PsiElement element = ref.getElement();
            // For each usage, determine if the usage is from the same class, same package, etc.
            PsiClass usageClass = findContainingClass(element);
            if (usageClass == null) {
                // If we can’t figure it out, err on the side of "public" usage
                info.setPublicUsageFound(true);
                break;
            } else {
                // Compare usage class vs. method class
                PsiClass methodClass = method.getContainingClass();
                assert methodClass != null;
                if (!inSameClass(methodClass, usageClass)) {
                    if (inSamePackage(methodClass, usageClass)) {
                        info.setPackagePrivateUsageFound(true);
                    } else if (isSubclass(methodClass, usageClass)) {
                        info.setProtectedUsageFound(true);
                    } else {
                        info.setPublicUsageFound(true);
                        break; // can't do better than public
                    }
                }
            }
        }

        return info;
    }

    private static PsiClass findContainingClass(PsiElement element) {
        while (element != null) {
            if (element instanceof PsiClass) {
                return (PsiClass) element;
            }
            element = element.getParent();
        }
        return null;
    }

    private static boolean inSameClass(PsiClass c1, PsiClass c2) {
        return c1.equals(c2);
    }

    private static boolean inSamePackage(PsiClass c1, PsiClass c2) {
        PsiFile f1 = c1.getContainingFile();
        PsiFile f2 = c2.getContainingFile();
        if (f1 instanceof PsiJavaFile && f2 instanceof PsiJavaFile) {
            String p1 = ((PsiJavaFile) f1).getPackageName();
            String p2 = ((PsiJavaFile) f2).getPackageName();
            return p1.equals(p2);
        }
        return false;
    }

    private static boolean isSubclass(PsiClass base, PsiClass maybeSubclass) {
        // Very naive check. For a real check you'd do something like:
        return maybeSubclass.isInheritor(base, true);
    }

    /**
     * Returns the textual visibility of the method (public/protected/package-private/private).
     */
    private static String getVisibility(PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();
        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
            return PsiModifier.PUBLIC;
        } else if (modifierList.hasModifierProperty(PsiModifier.PROTECTED)) {
            return PsiModifier.PROTECTED;
        } else if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            return PsiModifier.PRIVATE;
        } else {
            // “Default” means package-private in Java
            return "package-private";
        }
    }
}