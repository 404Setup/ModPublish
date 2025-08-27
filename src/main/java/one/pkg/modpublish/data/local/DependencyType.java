package one.pkg.modpublish.data.local;

public enum DependencyType {
    EMBEDDED("Embedded", "embeddedLibrary"),
    REQUIRED("Required", "requiredDependency"),
    OPTIONAL("Optional", "optionalDependency"),
    INCOMPATIBLE("Incompatible", "incompatible");

    private final String displayName;
    private final String curseForgeName;

    DependencyType(String displayName, String curseForgeName) {
        this.displayName = displayName;
        this.curseForgeName = curseForgeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCurseForgeName() {
        return curseForgeName;
    }

    public String getModrinthName() {
        return displayName.toLowerCase();
    }

    @Override
    public String toString() {
        return displayName;
    }
}