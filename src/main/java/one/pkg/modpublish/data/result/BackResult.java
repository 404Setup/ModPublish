package one.pkg.modpublish.data.result;

public record BackResult(String result) implements Result {
    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return !isSuccess();
    }

    public static BackResult result(String result) {
        return new BackResult(result);
    }
}
