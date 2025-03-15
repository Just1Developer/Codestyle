package net.justonedev.codestyle.checks;

public class VisibilityInfo {
    private final String currentLevel;

    // Flags that tell us if we need at least that level
    private boolean publicUsageFound = false;
    private boolean protectedUsageFound = false;
    private boolean packagePrivateUsageFound = false;

    public VisibilityInfo(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public void setPublicUsageFound(boolean value) {
        publicUsageFound = publicUsageFound || value;
    }

    public void setProtectedUsageFound(boolean value) {
        protectedUsageFound = protectedUsageFound || value;
    }

    public void setPackagePrivateUsageFound(boolean value) {
        packagePrivateUsageFound = packagePrivateUsageFound || value;
    }

    /**
     * Returns whether we can lower the visibility from the current level.
     */
    public boolean canLowerVisibility() {
        String suggested = getSuggestedLevel();
        // If the suggested level is the same as the current, no, we can’t lower it.
        return !suggested.equals(currentLevel);
    }

    /**
     * A naive approach to figure out the minimal needed visibility.
     */
    public String getSuggestedLevel() {
        if (publicUsageFound) {
            return "public";
        } else if (protectedUsageFound) {
            return "protected";
        } else if (packagePrivateUsageFound) {
            return "package-private";
        } else {
            return "private";
        }
    }
}
