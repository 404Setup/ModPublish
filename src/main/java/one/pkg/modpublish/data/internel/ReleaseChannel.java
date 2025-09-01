package one.pkg.modpublish.data.internel;

public enum ReleaseChannel {
    Release("release"), Beta("beta"), Alpha("alpha");

    private final String type;

    ReleaseChannel(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
