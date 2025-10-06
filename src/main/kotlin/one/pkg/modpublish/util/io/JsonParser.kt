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
package one.pkg.modpublish.util.io

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.*
import java.lang.reflect.Type

@Suppress("unused")
object JsonParser {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @Throws(JsonParseException::class)
    fun getJsonObject(reader: Reader): JsonObject {
        try {
            val jsonReader = JsonReader(reader)
            jsonReader.strictness = Strictness.STRICT
            return gson.getAdapter(JsonObject::class.java).read(jsonReader)
                ?: throw JsonParseException("JSON data was null or empty")
        } catch (e: IOException) {
            throw JsonParseException(e)
        }
    }

    @Throws(JsonParseException::class)
    fun InputStream.getJsonObject(): JsonObject = getJsonObject(InputStreamReader(this))

    fun Any.toJson(): String = gson.toJson(this)

    fun Any.toJson(writer: Writer) = gson.toJson(this, writer)

    fun String.fromJson(): JsonObject = gson.fromJson(this, JsonObject::class.java)

    fun <T> String.fromJson(classOfT: Class<T>): T = gson.fromJson(this, classOfT)

    fun <T> String.fromJson(typeToken: TypeToken<T>): T = gson.fromJson(this, typeToken.type)

    fun <T> String.fromJson(typeOfT: Type): T = gson.fromJson(this, typeOfT)

    fun <T> JsonElement.fromJson(typeToken: TypeToken<T>): T = gson.fromJson(this, typeToken.type)

    fun <T> JsonElement.fromJson(typeOfT: Type): T = gson.fromJson(this, typeOfT)

    @Throws(JsonParseException::class)
    fun Reader.fromJson(): JsonObject = gson.fromJson(this, JsonObject::class.java)

    @Throws(JsonParseException::class)
    fun <T> Reader.fromJson(typeToken: TypeToken<T>): T = gson.fromJson(this, typeToken.type)

    @Throws(JsonParseException::class)
    fun <T> Reader.fromJson(typeOfT: Type): T = gson.fromJson(this, typeOfT)

    @Throws(JsonParseException::class)
    fun <T> Reader.fromJson(classOfT: Class<T>): T = gson.fromJson(this, classOfT)

    @Throws(JsonParseException::class)
    fun InputStream.fromJson(): JsonObject = gson.fromJson(InputStreamReader(this), JsonObject::class.java)

    @Throws(JsonParseException::class)
    fun <T> InputStream.fromJson(classOfT: Class<T>): T = gson.fromJson(InputStreamReader(this), classOfT)

    @Throws(JsonParseException::class)
    fun <T> InputStream.fromJson(typeOfT: Type): T = gson.fromJson(InputStreamReader(this), typeOfT)

    @Throws(JsonParseException::class)
    fun <T> InputStream.fromJson(typeToken: TypeToken<T>): T = gson.fromJson(InputStreamReader(this), typeToken.type)
}
