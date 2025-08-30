package one.pkg.modpublish.data.internel;

public record Info(String data, boolean failed, boolean globalData) {
    public static final Info INSTANCE = new Info("", false, false);

    public static Info of(String data, boolean failed, boolean globalData) {
        return new Info(data, failed, globalData);
    }

    public static Info of(String data) {
        return new Info(data, false, false);
    }
}