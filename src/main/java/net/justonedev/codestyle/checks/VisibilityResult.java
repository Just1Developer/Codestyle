package net.justonedev.codestyle.checks;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;

public record VisibilityResult(PsiClass psiClass, PsiMethod psiMethod, Visibility oldVisibility, Visibility newVisibility) {
    public String getClassName() {
        return psiClass != null ? psiClass.getQualifiedName() : "Anonymous Class";
    }

    public String getMethodName() {
        return psiMethod != null ? psiMethod.getName() : "Anonymous Method";
    }

    public PsiFile getFile() {
        return psiClass != null ? psiClass.getContainingFile() : null;
    }

    /**
     * Returns the text offset for the PSI element, usually on the method/class name.
     * -1 if neither class nor method is set.
     */
    public int getOffset() {
        if (psiMethod != null) {
            // Prefer the method name offset, otherwise fall back to overall method offset
            var nameId = psiMethod.getNameIdentifier();
            return (nameId != null) ? nameId.getTextOffset() : psiMethod.getTextOffset();
        } else if (psiClass != null) {
            // Prefer the class name offset, otherwise fall back to overall class offset
            var nameId = psiClass.getNameIdentifier();
            return (nameId != null) ? nameId.getTextOffset() : psiClass.getTextOffset();
        }
        return 0;
    }
}
