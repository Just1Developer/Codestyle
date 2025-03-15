package net.justonedev.codestyle.checks;

public record VisibilityResult(String className, String methodName, Visibility oldVisibility, Visibility newVisibility) {
}
