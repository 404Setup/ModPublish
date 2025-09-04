/*
 * Copyright (C) 2025 404Setup (https://github.com/404Setup)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package one.pkg.modpublish.util.io;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
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

    public static JsonObject fromJson(String json) {
        return gson.fromJson(json, JsonObject.class);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        return gson.fromJson(json, typeToken.getType());
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static JsonObject fromJson(Reader reader) throws JsonParseException {
        return gson.fromJson(reader, JsonObject.class);
    }

    public static <T> T fromJson(Reader reader, TypeToken<T> typeToken) throws JsonParseException {
        return gson.fromJson(reader, typeToken.getType());
    }

    public static <T> T fromJson(Reader reader, Type typeOfT) throws JsonParseException {
        return gson.fromJson(reader, typeOfT);
    }

    public static <T> T fromJson(Reader reader, Class<T> classOfT) throws JsonParseException {
        return gson.fromJson(reader, classOfT);
    }

    public static JsonObject fromJson(InputStream in) throws JsonParseException {
        return gson.fromJson(new InputStreamReader(in), JsonObject.class);
    }

    public static <T> T fromJson(InputStream in, Class<T> classOfT) throws JsonParseException {
        return gson.fromJson(new InputStreamReader(in), classOfT);
    }

    public static <T> T fromJson(InputStream in, Type typeOfT) throws JsonParseException {
        return gson.fromJson(new InputStreamReader(in), typeOfT);
    }

    public static <T> T fromJson(InputStream in, TypeToken<T> typeToken) throws JsonParseException {
        return gson.fromJson(new InputStreamReader(in), typeToken.getType());
    }

}
