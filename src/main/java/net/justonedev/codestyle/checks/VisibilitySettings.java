package net.justonedev.codestyle.checks;

public record VisibilitySettings(PackagePrivateStatus usePackagePrivate, ProtectedStatus useProtected) {

    public static final VisibilitySettings DEFAULT = new VisibilitySettings(
            PackagePrivateStatus.NEVER, ProtectedStatus.WHEN_INHERITED
    );

    public boolean shouldApplyVisibilityHint(Visibility fromVis, Visibility toVis) {
        if (toVis.getLevel() >= fromVis.getLevel()
                || toVis == Visibility.PACKAGE_PRIVATE && usePackagePrivate == PackagePrivateStatus.NEVER) {
            return false;
        }
        return toVis != Visibility.PROTECTED || useProtected != ProtectedStatus.NEVER;
    }

    public VisibilitySettings(String usePackagePrivate, String useProtected) {
        this(PackagePrivateStatus.get(usePackagePrivate), ProtectedStatus.get(useProtected));
    }

    public enum PackagePrivateStatus {
        USE, NEVER;

        static PackagePrivateStatus get(String value) {
            return value.equals("use") ? USE : NEVER;
        }
    }
    public enum ProtectedStatus {
        USE, WHEN_INHERITED, NEVER;

        static ProtectedStatus get(String value) {
            return switch (value) {
                case "use" -> USE;
                case "never" -> NEVER;
                default -> WHEN_INHERITED;
            };
        }
    }
}
