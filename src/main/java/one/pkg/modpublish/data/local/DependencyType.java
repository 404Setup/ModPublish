package one.pkg.modpublish.data.local;

public enum DependencyType {
    EMBEDDED("Embedded", "embeddedLibrary"),
    REQUIRED("Required", "requiredDependency"),
    OPTIONAL("Optional", "optionalDependency"),
    INCOMPATIBLE("Incompatible", "incompatible");

    private final String displayName;
    private final String curseforgeName;

    DependencyType(String displayName, String curseforgeName) {
        this.displayName = displayName;
        this.curseforgeName = curseforgeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCurseforgeName() {
        return curseforgeName;
    }

    public String getModrinthName() {
        return displayName.toLowerCase();
    }

    @Override
    public String toString() {
        return displayName;
    }
}