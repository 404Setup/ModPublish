package one.pkg.modpublish.data.result;

import one.pkg.modpublish.util.resources.Lang;
import org.jetbrains.annotations.PropertyKey;

public record PublishResult(String result) implements Result {
    public static final PublishResult EMPTY = new PublishResult("");

    public static PublishResult of(@PropertyKey(resourceBundle = Lang.File) String result) {
        return new PublishResult(Lang.get(result));
    }

    public static PublishResult of(@PropertyKey(resourceBundle = Lang.File) String result, Object... params) {
        return new PublishResult(Lang.get(result, params));
    }

    public static PublishResult create(String result) {
        return new PublishResult(result);
    }

    public static PublishResult empty() {
        return EMPTY;
    }

    public boolean isSuccess() {
        return result == null || result.trim().isEmpty();
    }

    public boolean isFailure() {
        return !isSuccess();
    }
}