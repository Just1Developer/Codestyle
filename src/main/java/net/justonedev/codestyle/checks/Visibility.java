package net.justonedev.codestyle.checks;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;

public enum Visibility {
    PUBLIC(3, "public"),
    PROTECTED(2, "protected"),
    PACKAGE_PRIVATE(1, "package-private"),
    PRIVATE(0, "private");

    private final int level;
    private final String visibility;

    Visibility(int level, String visibility) {
        this.level = level;
        this.visibility = visibility;
    }

    public int getLevel() {
        return level;
    }

    public String getVisibility() {
        return visibility;
    }

    public static Visibility fromLevel(int level) {
        for (Visibility visibility : values()) {
            if (visibility.level == level) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Unknown level: " + level);
    }

    private static Visibility fromString(String vis) {
        for (Visibility visibility : values()) {
            if (visibility.visibility.equals(vis)) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Unknown visibility: " + vis);
    }

    public static Visibility fromMethod(PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();
        String vis;
        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
            vis = PsiModifier.PUBLIC;
        } else if (modifierList.hasModifierProperty(PsiModifier.PROTECTED)) {
            vis = PsiModifier.PROTECTED;
        } else if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            vis = PsiModifier.PRIVATE;
        } else {
            // “Default” means package-private in Java
            vis = "package-private";
        }
        return fromString(vis);
    }

    @Override
    public String toString() {
        return visibility;
    }
}
