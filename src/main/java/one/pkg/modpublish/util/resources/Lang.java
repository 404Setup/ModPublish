package one.pkg.modpublish.util.resources;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

@NonNls
public class Lang extends DynamicBundle {
    public static final String File = "messages.ModPublish";
    private static final Lang INSTANCE = new Lang();

    private Lang() {
        super(File);
    }

    public static String get(@PropertyKey(resourceBundle = File) String key) {
        return INSTANCE.getMessage(key);
    }

    public static String get(@PropertyKey(resourceBundle = File) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}