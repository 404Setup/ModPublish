package one.pkg.modpublish.data.properties;

public record Info(String data, boolean globalData) {
    public static final Info INSTANCE = new Info("", false);

    public static Info of(String data, boolean globalData) {
        return new Info(data, globalData);
    }

    public static Info of(String data) {
        return new Info(data, false);
    }
}