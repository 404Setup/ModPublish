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
import one.pkg.modpublish.data.internal.SideType
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

    @Throws(AssertionError::class, IllegalStateException::class)
    fun getFabric(): LocalModInfo {
        val range = json.get("depends").asJsonObject.get("minecraft")
        val finalRange = runCatching {
            range.asString
        }.onFailure {
            val arr = range.asJsonArray
            val arrMain = ArrayList<String>(arr.size())
            arr.forEach { arrMain.add(it.asString) }
            arrMain.reverse()
            buildString {
                append("[")
                append(arrMain.joinToString(","))
                append("]")
            }
        }.getOrDefault("")

        val side = when (json.get("environment").asString) {
            "*" -> SideType.BOTH
            "client" -> SideType.CLIENT
            else -> SideType.SERVER
        }

        return LocalModInfo(
            name = json.get("name").asString,
            version = json.get("version").asString,
            versionRange = finalRange,
            sideType = side
        )
    }

    @Throws(AssertionError::class, IllegalStateException::class)
    fun getLiteMod(): LocalModInfo =
        LocalModInfo(
            name = json.get("name").asString,
            version = json.get("version").asString,
            versionRange = json.get("mcversion").asString
        )

    @Throws(AssertionError::class, IllegalStateException::class)
    fun getRiftMod(): LocalModInfo =
        LocalModInfo(
            name = json.get("name").asString,
            version = json.get("version").let {
                runCatching { it.asString }.getOrDefault("1.0.0")
            },
            versionRange = "1.13"
        )

    @Throws(AssertionError::class, IllegalStateException::class)
    fun getMcMod(): LocalModInfo {
        val obj = json.asJsonArray[0].asJsonObject

        val side = when {
            obj.get("serverSideOnly")?.asBoolean == true -> SideType.SERVER
            obj.get("clientSideOnly")?.asBoolean == true -> SideType.CLIENT
            else -> SideType.BOTH
        }

        return LocalModInfo(
            name = obj.get("name").asString,
            version = obj.get("version").asString,
            versionRange = obj.get("mcversion").asString,
            sideType = side
        )
    }
}
