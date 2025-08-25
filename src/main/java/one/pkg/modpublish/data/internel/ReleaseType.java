package one.pkg.modpublish.data.internel;

public enum ReleaseType {
    Release("release"), Beta("beta"), Alpha("alpha");
    private final String type;

    ReleaseType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
