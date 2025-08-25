package one.pkg.modpublish.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class JsonParser {
    private static final Gson gson = new Gson();

    public static JsonObject getJsonObject(Reader reader) throws JsonParseException {
        try {
            JsonReader jsonreader = new JsonReader(reader);
            jsonreader.setStrictness(Strictness.STRICT);
            JsonObject t = gson.getAdapter(JsonObject.class).read(jsonreader);
            if (t == null) {
                throw new JsonParseException("JSON data was null or empty");
            } else {
                return t;
            }
        } catch (IOException ioexception) {
            throw new JsonParseException(ioexception);
        }
    }

    public static JsonObject getJsonObject(InputStream in) throws JsonParseException {
        return getJsonObject(new InputStreamReader(in));
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(Reader reader, Class<T> classOfT) throws JsonParseException {
        return gson.fromJson(reader, classOfT);
    }

    public static <T> T fromJson(InputStream in, Class<T> classOfT) throws JsonParseException {
        return gson.fromJson(new InputStreamReader(in), classOfT);
    }
}
