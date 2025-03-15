package net.justonedev.codestyle.checks;

public class VisibilityInfo {
    private final Visibility currentLevel;

    // Flags that tell us if we need at least that level
    private boolean publicUsageFound = false;
    private boolean protectedUsageFound = false;
    private boolean packagePrivateUsageFound = false;

    public VisibilityInfo(Visibility currentLevel) {
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
        return canLowerVisibility(VisibilitySettings.DEFAULT);
    }

    /**
     * Returns whether we can lower the visibility from the current level.
     */
    public boolean canLowerVisibility(VisibilitySettings settings) {
        Visibility suggested = getSuggestedLevel(settings);
        // If the suggested level is the same as the current, no, we can’t lower it.
        return suggested.getLevel() < currentLevel.getLevel();
    }

    public Visibility getSuggestedLevel() {
        return getSuggestedLevel(VisibilitySettings.DEFAULT);
    }

    /**
     * A naive approach to figure out the minimal needed visibility.
     */
    public Visibility getSuggestedLevel(VisibilitySettings settings) {
        int newLevel;
        if (publicUsageFound) {
            newLevel = 3;
        } else if (protectedUsageFound
                || packagePrivateUsageFound
                && settings.useProtected() == VisibilitySettings.ProtectedStatus.USE
                && settings.usePackagePrivate() == VisibilitySettings.PackagePrivateStatus.NEVER) {
            newLevel = 2;
        } else if (packagePrivateUsageFound) {
            newLevel = 1;
        } else {
            newLevel = 0;
        }
        Visibility newVis = Visibility.fromLevel(newLevel);
        return settings.shouldApplyVisibilityHint(currentLevel, newVis) ? newVis : currentLevel;
    }
}
