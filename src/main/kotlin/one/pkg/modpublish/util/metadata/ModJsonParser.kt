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
package one.pkg.modpublish.util.metadata

import com.google.gson.JsonObject
import one.pkg.modpublish.data.internal.LocalModInfo
import one.pkg.modpublish.util.io.JsonParser
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class ModJsonParser(inputStream: InputStream) {
    private val json: JsonObject

    init {
        val reader: Reader = InputStreamReader(inputStream)
        json = JsonParser.getJsonObject(reader)
    }

    fun getFabric(): LocalModInfo? = try {
        LocalModInfo(
            json.get("name").asString, json.get("version").asString,
            json.get("depends").asJsonObject.get("minecraft").asString
        )
    } catch (_: Exception) {
        null
    }

    fun getLiteMod(): LocalModInfo? = try {
        LocalModInfo(
            json.get("name").asString, json.get("version").asString,
            json.get("mcversion").asString
        )
    } catch (_: Exception) {
        null
    }

    fun getRiftMod(): LocalModInfo? = try {
        LocalModInfo(
            json.get("name").asString, json.get("version").let {
                try {
                    it.asString
                } catch (_: Exception) {
                    "1.0.0"
                }
            },
            "1.13"
        )
    } catch (_: Exception) {
        null
    }

    fun getMcMod(): LocalModInfo? = try {
        val obj = json.asJsonArray.get(0).asJsonObject
        LocalModInfo(
            obj.get("name").asString, obj.get("version").asString,
            obj.get("mcversion").asString
        )
    } catch (_: Exception) {
        null
    }
}
