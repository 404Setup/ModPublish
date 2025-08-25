package one.pkg.modpublish.data.modinfo;

import com.google.gson.JsonObject;
import one.pkg.modpublish.util.JsonParser;
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
    public String getVersion() {
        try {
            return json.get("version").getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}
