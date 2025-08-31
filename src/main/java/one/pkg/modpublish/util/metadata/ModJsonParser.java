package one.pkg.modpublish.util.metadata;

import com.google.gson.JsonObject;
import one.pkg.modpublish.data.internel.LocalModInfo;
import one.pkg.modpublish.util.io.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ModJsonParser {
    private final JsonObject json;

    public ModJsonParser(InputStream inputStream) {
        Reader reader = new InputStreamReader(inputStream);

        json = JsonParser.getJsonObject(reader);
    }

    @Nullable
    public LocalModInfo get() {
        try {
            return new LocalModInfo(json.get("name").getAsString(), json.get("version").getAsString());
        } catch (Exception e) {
            return null;
        }
    }
}
