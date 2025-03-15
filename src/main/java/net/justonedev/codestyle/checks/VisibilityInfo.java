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

    public boolean isPublicUsageFound() {
        return publicUsageFound;
    }

    public boolean isProtectedUsageFound() {
        return protectedUsageFound;
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
        int currentLevelInt = getLevelInt(currentLevel);
        if (publicUsageFound) {
            return "public";
        } else if (protectedUsageFound && currentLevelInt >= 2) {
            return "protected";
        } else if (packagePrivateUsageFound && currentLevelInt >= 1) {
            return "package-private";
        } else {
            return "private";
        }
    }

    private int getLevelInt(String level) {
        return switch (level) {
            case "public" -> 3;
            case "protected" -> 2;
            case "package-private" -> 1;
            case "private" -> 0;
            default -> -1;
        };
    }
}
