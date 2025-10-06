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

package one.pkg.modpublish.util.io.toml

import com.intellij.openapi.diagnostic.Logger
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import one.pkg.modpublish.util.io.JsonParser.toJson
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

@Suppress("UNCHECKED_CAST", "UNUSED")
class KTomlParser internal constructor(val parsedData: MutableMap<String, Any?>) : Closeable {
    private var prebakedValue: Any? = null
    private var isPrebakedMode: Boolean = false

    private constructor(parsedData: MutableMap<String, Any?>, prebakedValue: Any?) : this(parsedData) {
        this.prebakedValue = prebakedValue
        this.isPrebakedMode = true
    }

    private inline fun <T> withPrebakedValue(defaultValue: T, action: (Any) -> T): T {
        if (!isPrebakedMode || prebakedValue == null) return defaultValue
        return action(prebakedValue!!)
    }

    private inline fun <T> withPrebakedValueOrDefault(defaultValue: T, action: (Any) -> T?): T {
        if (!isPrebakedMode || prebakedValue == null) return defaultValue
        return action(prebakedValue!!) ?: defaultValue
    }

    private fun canConvertToNumber(): Boolean {
        if (!isPrebakedMode) return false
        return when (val value = prebakedValue) {
            is Number -> true
            else -> value?.toString()?.let { str ->
                str.toIntOrNull() != null || str.toLongOrNull() != null || str.toDoubleOrNull() != null
            } ?: false
        }
    }

    private inline fun <T : Number> convertToNumber(
        defaultValue: T,
        intConverter: (Int) -> T,
        longConverter: (Long) -> T,
        doubleConverter: (Double) -> T,
        stringConverter: (String) -> T?
    ): T {
        return withPrebakedValueOrDefault(defaultValue) { value ->
            when (value) {
                is Int -> intConverter(value)
                is Long -> longConverter(value)
                is Double -> doubleConverter(value)
                is Number -> doubleConverter(value.toDouble())
                else -> stringConverter(value.toString())
            }
        }
    }

    fun getAsString(): String {
        return withPrebakedValue("") { it.toString() }
    }

    fun getAsInt(): Int {
        return convertToNumber(
            defaultValue = 0,
            intConverter = { it },
            longConverter = { it.toInt() },
            doubleConverter = { it.toInt() },
            stringConverter = { it.toIntOrNull() }
        )
    }

    fun getAsLong(): Long {
        return convertToNumber(
            defaultValue = 0L,
            intConverter = { it.toLong() },
            longConverter = { it },
            doubleConverter = { it.toLong() },
            stringConverter = { it.toLongOrNull() }
        )
    }

    fun getAsDouble(): Double {
        return convertToNumber(
            defaultValue = 0.0,
            intConverter = { it.toDouble() },
            longConverter = { it.toDouble() },
            doubleConverter = { it },
            stringConverter = { it.toDoubleOrNull() }
        )
    }

    fun getAsBoolean(): Boolean {
        return withPrebakedValueOrDefault(false) { value ->
            when (value) {
                is Boolean -> value
                else -> value.toString().toBooleanStrictOrNull()
            }
        }
    }

    fun getAsMap(): Map<String, Any?> {
        return withPrebakedValueOrDefault(emptyMap()) { value ->
            (value as? Map<*, *>)?.let { it as Map<String, Any?> }
        }
    }

    fun getAsObject(): KTomlParser {
        return withPrebakedValueOrDefault(empty()) { value ->
            when (value) {
                is Map<*, *> -> KTomlParser(value as MutableMap<String, Any?>)
                is List<*> -> {
                    val list = value as? List<Map<String, Any?>>
                    if (list?.isNotEmpty() == true) {
                        KTomlParser(list[0] as MutableMap<String, Any?>)
                    } else null
                }

                else -> null
            }
        }
    }

    fun getAsList(): List<Any?> {
        return withPrebakedValueOrDefault(emptyList()) { value ->
            (value as? List<*>)
        }
    }

    fun asPrebaked(key: String): KTomlParser {
        val pathSegments = parsePath(key)
        val value = if (pathSegments.size == 1) {
            parsedData[key]
        } else {
            navigateToPath(pathSegments, 0)?.parsedData?.get(pathSegments.last())
        }
        return KTomlParser(mutableMapOf(), value)
    }

    fun asPrebakedPath(vararg pathSegments: String): KTomlParser {
        var current: Any? = parsedData
        for (segment in pathSegments) {
            current = when (current) {
                is Map<*, *> -> (current as Map<String, Any?>)[segment]
                is List<*> -> {
                    val list = current as List<Map<String, Any?>>
                    if (list.isNotEmpty()) list[0][segment] else null
                }

                else -> null
            }
            if (current == null) break
        }
        return KTomlParser(mutableMapOf(), current)
    }

    fun canGetAsString(): Boolean {
        return isPrebakedMode && prebakedValue != null
    }

    fun canGetAsInt(): Boolean {
        return canConvertToNumber() && withPrebakedValueOrDefault(false) { value ->
            when (value) {
                is Number -> true
                else -> value.toString().toIntOrNull() != null
            }
        }
    }

    fun canGetAsLong(): Boolean {
        return canConvertToNumber() && withPrebakedValueOrDefault(false) { value ->
            when (value) {
                is Number -> true
                else -> value.toString().toLongOrNull() != null
            }
        }
    }

    fun canGetAsDouble(): Boolean {
        return canConvertToNumber() && withPrebakedValueOrDefault(false) { value ->
            when (value) {
                is Number -> true
                else -> value.toString().toDoubleOrNull() != null
            }
        }
    }

    fun canGetAsBoolean(): Boolean {
        return withPrebakedValueOrDefault(false) { value ->
            when (value) {
                is Boolean -> true
                else -> {
                    val str = value.toString().lowercase()
                    str == "true" || str == "false"
                }
            }
        }
    }

    fun canGetAsMap(): Boolean {
        return withPrebakedValueOrDefault(false) { value ->
            value is Map<*, *>
        }
    }

    fun canGetAsObject(): Boolean {
        return withPrebakedValueOrDefault(false) { value ->
            value is Map<*, *> || value is List<*>
        }
    }

    fun canGetAsList(): Boolean {
        return withPrebakedValueOrDefault(false) { value ->
            value is List<*>
        }
    }

    fun isPrebakedMode(): Boolean = isPrebakedMode

    fun getPrebakedValue(): Any? = if (isPrebakedMode) prebakedValue else null

    fun getAsArrayElement(index: Int): KTomlParser {
        return withPrebakedValueOrDefault(empty()) { value ->
            (value as? List<*>)?.let { list ->
                if (index >= 0 && index < list.size) {
                    KTomlParser(mutableMapOf(), list[index])
                } else null
            }
        }
    }

    fun getPrebakedSize(): Int {
        return withPrebakedValue(0) { value ->
            when (value) {
                is List<*> -> value.size
                is Map<*, *> -> value.size
                else -> 1
            }
        }
    }

    fun toJson(): String {
        return parsedData.toJson()
    }

    /**
     * Convert to TOML string format
     */
    fun toTomlString(): String {
        return serializeToToml(parsedData)
    }

    /**
     * Serialize the data to TOML format (similar to toString but specifically for TOML)
     */
    private fun serializeToToml(data: Map<String, Any?>, prefix: String = ""): String {
        val result = StringBuilder()
        val tables = mutableMapOf<String, Any?>()
        val arrayTables = mutableMapOf<String, List<Map<String, Any?>>>()

        for ((key, value) in data) {
            when (value) {
                is Map<*, *> -> {
                    tables[key] = value as Map<String, Any?>
                }

                is List<*> -> {
                    if (value.isNotEmpty() && value.all { it is Map<*, *> }) {
                        arrayTables[key] = value as List<Map<String, Any?>>
                    } else {
                        result.append(serializeKeyValue(key, value))
                    }
                }

                else -> {
                    result.append(serializeKeyValue(key, value))
                }
            }
        }

        for ((key, tableData) in tables) {
            if (result.isNotEmpty()) result.append("\n")
            val tableName = if (prefix.isEmpty()) key else "$prefix.$key"
            result.append("[$tableName]\n")
            result.append(serializeToToml(tableData as Map<String, Any?>, tableName))
        }

        for ((key, arrayData) in arrayTables) {
            for (tableData in arrayData) {
                if (result.isNotEmpty()) result.append("\n")
                val tableName = if (prefix.isEmpty()) key else "$prefix.$key"
                result.append("[[$tableName]]\n")
                result.append(serializeToToml(tableData, tableName))
            }
        }

        return result.toString()
    }

    private fun serializeKeyValue(key: String, value: Any?): String {
        val serializedValue = when (value) {
            null -> "\"\""
            is String -> "\"${escapeTomlString(value)}\""
            is Boolean -> value.toString()
            is Number -> value.toString()
            is LocalDateTime -> "\"$value\""
            is LocalDate -> "\"$value\""
            is LocalTime -> "\"$value\""
            is List<*> -> "[${value.joinToString(", ") { serializeValue(it) }}]"
            else -> "\"${escapeTomlString(value.toString())}\""
        }
        return "$key = $serializedValue\n"
    }

    private fun serializeValue(value: Any?): String {
        return when (value) {
            null -> "\"\""
            is String -> "\"${escapeTomlString(value)}\""
            is Boolean -> value.toString()
            is Number -> value.toString()
            is LocalDateTime -> "\"$value\""
            is LocalDate -> "\"$value\""
            is LocalTime -> "\"$value\""
            else -> "\"${escapeTomlString(value.toString())}\""
        }
    }

    private fun escapeTomlString(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Get a value by key, returns null if not found
     */
    fun get(key: String): Any? {
        return parsedData[key]
    }

    /**
     * Check if a key exists
     */
    fun has(key: String): Boolean {
        return parsedData.containsKey(key)
    }

    /**
     * Navigate through nested path using parsed path segments
     */
    private fun navigateToPath(pathSegments: Array<String>, startIndex: Int): KTomlParser? {
        if (startIndex >= pathSegments.size) {
            return this
        }

        val currentSegment = pathSegments[startIndex]
        return when (val value = parsedData[currentSegment]) {
            is Map<*, *> -> {
                KTomlParser(value as MutableMap<String, Any?>).navigateToPath(pathSegments, startIndex + 1)
            }

            is List<*> -> {
                val list = value as List<MutableMap<String, Any?>>
                if (list.isNotEmpty()) {
                    KTomlParser(list[0]).navigateToPath(pathSegments, startIndex + 1)
                } else null
            }

            else -> if (startIndex == pathSegments.size - 1) this else null
        }
    }

    private inline fun <T> withPathNavigation(
        key: String,
        action: (KTomlParser, String) -> T
    ): T? {
        val pathSegments = parsePath(key)
        return if (pathSegments.size == 1) {
            action(this, key)
        } else {
            val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
            navigateToPath(parentPath, 0)?.let { parent ->
                action(parent, pathSegments.last())
            }
        }
    }

    fun canAsTomlParser(key: String): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is Map<*, *> -> true
                is List<*> -> value.isNotEmpty() && value[0] is Map<*, *>
                else -> false
            }
        } ?: false
    }

    fun getAsTomlParser(key: String): KTomlParser? {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is Map<*, *> -> {
                    val mutableValue = if (value is MutableMap<*, *>) {
                        value as MutableMap<String, Any?>
                    } else {
                        HashMap(value as Map<String, Any?>)
                    }
                    KTomlParser(mutableValue)
                }

                is List<*> -> {
                    val list = value as List<Map<String, Any?>>
                    if (list.isNotEmpty()) {
                        val firstMap = list[0]
                        val mutableValue = if (firstMap is MutableMap<*, *>) {
                            firstMap as MutableMap<String, Any?>
                        } else {
                            HashMap(firstMap)
                        }
                        KTomlParser(mutableValue)
                    } else null
                }

                else -> null
            }
        }
    }

    fun getAsTomlArray(key: String): KTomlArray {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is List<*> -> KTomlArray(value as List<Map<String, Any?>>)
                is Map<*, *> -> KTomlArray(listOf(value as Map<String, Any?>))
                else -> KTomlArray(emptyList())
            }
        } ?: KTomlArray(emptyList())
    }

    fun canAsTomlArray(key: String): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            val value = parser.parsedData[localKey]
            value is List<*> || value is Map<*, *>
        } ?: false
    }

    fun getAsString(key: String, defaultValue: String = ""): String {
        return withPathNavigation(key) { parser, localKey ->
            parser.parsedData[localKey]?.toString() ?: defaultValue
        } ?: defaultValue
    }

    fun canAsString(key: String): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            val value = parser.parsedData[localKey]
            value != null && value !is List<*> && value !is Map<*, *>
        } ?: false
    }

    fun getAsInt(key: String, defaultValue: Int = 0): Int {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is Number -> value.toInt()
                else -> value?.toString()?.toIntOrNull() ?: defaultValue
            }
        } ?: defaultValue
    }

    fun canAsInt(key: String): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            val value = parser.parsedData[localKey] ?: return@withPathNavigation false
            value is Number || value.toString().toIntOrNull() != null
        } ?: false
    }

    fun getAsLong(key: String, defaultValue: Long = 0L): Long {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is Number -> value.toLong()
                else -> value?.toString()?.toLongOrNull() ?: defaultValue
            }
        } ?: defaultValue
    }

    fun canAsLong(key: String): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            val value = parser.parsedData[localKey] ?: return@withPathNavigation false
            value is Number || value.toString().toLongOrNull() != null
        } ?: false
    }

    fun getAsDouble(key: String, defaultValue: Double = 0.0): Double {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is Number -> value.toDouble()
                else -> value?.toString()?.toDoubleOrNull() ?: defaultValue
            }
        } ?: defaultValue
    }

    fun canAsDouble(key: String): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            val value = parser.parsedData[localKey] ?: return@withPathNavigation false
            value is Number || value.toString().toDoubleOrNull() != null
        } ?: false
    }

    fun getAsBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is Boolean -> value
                else -> value?.toString()?.toBooleanStrictOrNull() ?: defaultValue
            }
        } ?: defaultValue
    }

    fun canAsBoolean(key: String): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            val value = parser.parsedData[localKey] ?: return@withPathNavigation false
            value is Boolean ||
                    value.toString().lowercase() == "true" ||
                    value.toString().lowercase() == "false"
        } ?: false
    }

    /**
     * Get all keys
     */
    fun keySet(): Set<String> {
        return parsedData.keys
    }

    /**
     * Get the size of this TOML object
     */
    fun size(): Int {
        return parsedData.size
    }

    /**
     * Check if this TOML object is empty
     */
    fun isEmpty(): Boolean {
        return parsedData.isEmpty()
    }

    /**
     * Get raw parsed data
     */
    fun asMap(): Map<String, Any?> {
        return Collections.unmodifiableMap(parsedData)
    }

    /**
     * Get raw parsed data
     */
    fun asMutableMap(): MutableMap<String, Any?> {
        return HashMap(parsedData)
    }

    /**
     * Set a value by key
     */
    fun put(key: String, value: Any?) {
        val pathSegments = parsePath(key)
        if (pathSegments.size == 1) {
            parsedData[key] = value
        } else {
            val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
            val parent = getOrCreateNestedMap(parentPath)
            parent[pathSegments.last()] = value
        }
    }

    /**
     * Set a string value
     */
    fun putString(key: String, value: String) {
        put(key, value)
    }

    /**
     * Set an integer value
     */
    fun putInt(key: String, value: Int) {
        put(key, value)
    }

    /**
     * Set a long value
     */
    fun putLong(key: String, value: Long) {
        put(key, value)
    }

    /**
     * Set a double value
     */
    fun putDouble(key: String, value: Double) {
        put(key, value)
    }

    /**
     * Set a boolean value
     */
    fun putBoolean(key: String, value: Boolean) {
        put(key, value)
    }

    /**
     * Set a nested object/section
     */
    fun putObject(key: String, parser: KTomlParser) {
        put(key, parser.parsedData)
    }

    /**
     * Set an array/list value
     */
    fun putArray(key: String, array: List<Any?>) {
        put(key, array)
    }

    /**
     * Add a value to an array (creates array if doesn't exist)
     */
    fun addToArray(key: String, value: Any?) {
        val pathSegments = parsePath(key)
        val parent = if (pathSegments.size == 1) {
            parsedData
        } else {
            val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
            getOrCreateNestedMap(parentPath)
        }

        val arrayKey = pathSegments.last()
        when (val existing = parent[arrayKey]) {
            is MutableList<*> -> (existing as MutableList<Any?>).add(value)
            null -> parent[arrayKey] = mutableListOf(value)
            else -> parent[arrayKey] = mutableListOf(existing, value)
        }
    }

    /**
     * Add a string to an array
     */
    fun addStringToArray(key: String, value: String) {
        addToArray(key, value)
    }

    /**
     * Add an integer to an array
     */
    fun addIntToArray(key: String, value: Int) {
        addToArray(key, value)
    }

    /**
     * Add a long to an array
     */
    fun addLongToArray(key: String, value: Long) {
        addToArray(key, value)
    }

    /**
     * Add a double to an array
     */
    fun addDoubleToArray(key: String, value: Double) {
        addToArray(key, value)
    }

    /**
     * Add a boolean to an array
     */
    fun addBooleanToArray(key: String, value: Boolean) {
        addToArray(key, value)
    }

    /**
     * Add an object to an array
     */
    fun addObjectToArray(key: String, parser: KTomlParser) {
        addToArray(key, parser.parsedData)
    }

    /**
     * Get array size
     */
    fun getArraySize(key: String): Int {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is List<*> -> value.size
                else -> 0
            }
        } ?: 0
    }

    /**
     * Remove from array at index
     */
    fun removeFromArray(key: String, index: Int): Any? {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is MutableList<*> -> {
                    if (index >= 0 && index < value.size) value.removeAt(index)
                    else null
                }

                else -> null
            }
        }
    }

    /**
     * Remove specific value from array
     */
    fun removeFromArray(key: String, value: Any?): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            when (val existing = parser.parsedData[localKey]) {
                is MutableList<*> -> existing.remove(value)
                else -> false
            }
        } ?: false
    }

    /**
     * Clear array (remove all elements)
     */
    fun clearArray(key: String) {
        withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is MutableList<*> -> value.clear()
                else -> parser.parsedData[localKey] = mutableListOf<Any?>()
            }
        }
    }

    /**
     * Set array element at index
     */
    fun setArrayElement(key: String, index: Int, value: Any?) {
        withPathNavigation(key) { parser, localKey ->
            when (val existing = parser.parsedData[localKey]) {
                is MutableList<*> -> {
                    val list = existing as MutableList<Any?>
                    if (index >= 0 && index < list.size) {
                        list[index] = value
                    }
                }

                else -> {
                    val newList = mutableListOf<Any?>()
                    for (i in 0..index) {
                        newList.add(if (i == index) value else null)
                    }
                    parser.parsedData[localKey] = newList
                }
            }
        }
    }

    /**
     * Get array element at index as KTomlParser
     */
    fun getArrayElement(key: String, index: Int): KTomlParser {
        return withPathNavigation(key) { parser, localKey ->
            when (val value = parser.parsedData[localKey]) {
                is List<*> -> {
                    if (index >= 0 && index < value.size) {
                        KTomlParser(mutableMapOf(), value[index])
                    } else {
                        empty()
                    }
                }

                else -> empty()
            }
        } ?: empty()
    }

    /**
     * Check if array contains value
     */
    fun arrayContains(key: String, value: Any?): Boolean {
        return withPathNavigation(key) { parser, localKey ->
            when (val existing = parser.parsedData[localKey]) {
                is List<*> -> existing.contains(value)
                else -> false
            }
        } ?: false
    }

    /**
     * Get index of value in array
     */
    fun arrayIndexOf(key: String, value: Any?): Int {
        return withPathNavigation(key) { parser, localKey ->
            when (val existing = parser.parsedData[localKey]) {
                is List<*> -> existing.indexOf(value)
                else -> -1
            }
        } ?: -1
    }

    /**
     * Remove a key
     */
    fun remove(key: String): Any? {
        val pathSegments = parsePath(key)
        return if (pathSegments.size == 1) {
            parsedData.remove(key)
        } else {
            val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
            navigateToPath(parentPath, 0)?.parsedData?.remove(pathSegments.last())
        }
    }

    /**
     * Clear all data
     */
    fun clear() {
        parsedData.clear()
    }

    /**
     * Create or get nested map for path
     */
    private fun getOrCreateNestedMap(pathSegments: Array<String>): MutableMap<String, Any?> {
        var currentMap = parsedData

        for (segment in pathSegments) {
            val existing = currentMap[segment]
            currentMap = if (existing is MutableMap<*, *>) {
                existing as MutableMap<String, Any?>
            } else {
                val newMap = mutableMapOf<String, Any?>()
                currentMap[segment] = newMap
                newMap
            }
        }

        return currentMap
    }

    /**
     * Write to file as TOML format
     */
    fun writeToFile(filePath: Path) {
        try {
            Files.writeString(filePath, toTomlString(), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            LOG.error("Failed to write TOML to file: $filePath", e)
            throw e
        }
    }

    /**
     * Write to file as TOML format
     */
    fun writeToFile(file: File) {
        writeToFile(file.toPath())
    }

    /**
     * Save to file (alias for writeToFile)
     */
    fun save(filePath: Path) {
        writeToFile(filePath)
    }

    /**
     * Save to file (alias for writeToFile)
     */
    fun save(file: File) {
        writeToFile(file)
    }

    /**
     * Write to Writer as TOML format
     */
    fun writeToWriter(writer: Writer) {
        try {
            writer.write(toTomlString())
            writer.flush()
        } catch (e: Exception) {
            LOG.error("Failed to write TOML to Writer", e)
            throw e
        }
    }

    /**
     * Write to OutputStream as TOML format
     */
    fun writeToStream(outputStream: OutputStream) {
        try {
            OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                writeToWriter(writer)
            }
        } catch (e: Exception) {
            LOG.error("Failed to write TOML to OutputStream", e)
            throw e
        }
    }

    override fun toString(): String {
        return "TomlParser{parsedData=$parsedData}"
    }

    override fun close() {
        parsedData.clear()
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(KTomlParser::class.java)

        private val ARRAY_TABLE_PATTERN: Pattern = Pattern.compile("^\\s*\\[\\[([^]]+)]]\\s*$")
        private val TABLE_PATTERN: Pattern = Pattern.compile("^\\s*\\[([^]]+)]\\s*$")
        private val KEY_VALUE_PATTERN: Pattern = Pattern.compile("^\\s*([^=]+)\\s*=\\s*(.+)\\s*$")
        private val MULTILINE_STRING_START_PATTERN: Pattern = Pattern.compile("^\\s*([^=]+)\\s*=\\s*'''(.*)$")
        private val MULTILINE_STRING_END_PATTERN: Pattern = Pattern.compile("^(.*)'''\\s*$")

        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        private const val MAX_NESTING_DEPTH = 50
        private const val MAX_CACHE_SIZE = 500 // Maximum cache entries
        private const val CACHE_CLEANUP_THRESHOLD = 0.8 // Cleanup when 80% full
        private const val CACHE_CLEANUP_RATIO = 0.3 // Remove 30% of entries during cleanup
        private val pathCache = ConcurrentHashMap<String, Array<String>>()

        @Volatile
        private var cacheAccessCounter = 0

        @Volatile
        private var cacheHitCounter = 0

        @Volatile
        private var cacheMissCounter = 0

        fun empty(): KTomlParser {
            return KTomlParser(HashMap())
        }

        fun fromValue(value: Any?): KTomlParser {
            return KTomlParser(HashMap(), value)
        }

        fun fromString(value: String): KTomlParser {
            return KTomlParser(HashMap(), value)
        }

        fun fromNumber(value: Number): KTomlParser {
            return KTomlParser(HashMap(), value)
        }

        fun fromBoolean(value: Boolean): KTomlParser {
            return KTomlParser(HashMap(), value)
        }

        fun fromToml(toml: String): KTomlParser {
            validateInput(toml)
            return KTomlParser(parseToml(toml))
        }

        fun fromFile(filePath: Path): KTomlParser {
            return try {
                val fileSize = Files.size(filePath)
                if (fileSize > MAX_FILE_SIZE) {
                    LOG.error("TOML file too large: $fileSize bytes (max: $MAX_FILE_SIZE)")
                    return empty()
                }
                val content = Files.readString(filePath, StandardCharsets.UTF_8)
                fromToml(content)
            } catch (e: Exception) {
                LOG.error("Failed to read TOML file: $filePath", e)
                empty()
            }
        }

        fun fromFile(file: File): KTomlParser {
            return fromFile(file.toPath())
        }

        fun fromReader(reader: Reader): KTomlParser {
            return try {
                val bufferedReader = reader as? BufferedReader ?: BufferedReader(reader)
                bufferedReader.use { br ->
                    val content = buildString {
                        br.lineSequence().forEach { line ->
                            append(line).append('\n')
                        }
                    }
                    fromToml(content)
                }
            } catch (e: Exception) {
                LOG.error("Failed to read TOML from Reader", e)
                empty()
            }
        }

        fun fromStream(inputStream: InputStream): KTomlParser {
            return fromReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        }

        private fun validateInput(content: String) {
            if (content.length > MAX_FILE_SIZE) {
                throw IllegalArgumentException("TOML content too large: ${content.length} characters (max: $MAX_FILE_SIZE)")
            }
        }

        /**
         * Helper method to navigate and create nested sections
         */
        private fun navigateToNestedSection(
            result: MutableMap<String, Any?>,
            pathSegments: Array<String>
        ): MutableMap<String, Any?> {
            var currentMap: MutableMap<String, Any?> = result

            for (i in 0 until pathSegments.size - 1) {
                val segment = pathSegments[i]
                val existing = currentMap[segment]
                currentMap = if (existing !is MutableMap<*, *>) {
                    mutableMapOf<String, Any?>().also { currentMap[segment] = it }
                } else {
                    existing as MutableMap<String, Any?>
                }
            }
            return currentMap
        }

        /**
         * Helper method to assign value to correct section
         */
        private fun assignValueToSection(
            result: MutableMap<String, Any?>,
            currentSection: MutableMap<String, Any?>?,
            inRootSection: Boolean,
            key: String,
            value: Any?
        ) {
            if (currentSection != null) {
                currentSection[key] = value
            } else if (inRootSection) {
                result[key] = value
            }
        }

        /**
         * Helper method to handle multiline string completion
         */
        private fun completeMultilineString(
            result: MutableMap<String, Any?>,
            currentSection: MutableMap<String, Any?>?,
            inRootSection: Boolean,
            multilineKey: String,
            multilineValue: StringBuilder
        ) {
            assignValueToSection(result, currentSection, inRootSection, multilineKey, multilineValue.toString())
        }

        private fun parseToml(content: String): MutableMap<String, Any?> {
            val result = mutableMapOf<String, Any?>()
            var currentSection: MutableMap<String, Any?>? = null
            var inRootSection = true

            val lines = content.split("\r?\n".toRegex())
            var inMultilineString = false
            var multilineValue = StringBuilder()
            var multilineKey: String? = null

            for (line in lines) {
                val trimmedLine = line.trim()

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    if (inMultilineString) {
                        multilineValue.append(line).append('\n')
                    }
                    continue
                }

                if (inMultilineString) {
                    val endMatcher = MULTILINE_STRING_END_PATTERN.matcher(line)
                    if (endMatcher.matches()) {
                        multilineValue.append(endMatcher.group(1))
                        if (multilineKey != null) {
                            completeMultilineString(result, currentSection, inRootSection, multilineKey, multilineValue)
                        }
                        inMultilineString = false
                        multilineValue = StringBuilder()
                        multilineKey = null
                    } else {
                        multilineValue.append(line).append('\n')
                    }
                    continue
                }

                val arrayTableMatcher = ARRAY_TABLE_PATTERN.matcher(trimmedLine)
                if (arrayTableMatcher.matches()) {
                    val sectionPath = arrayTableMatcher.group(1).trim()
                    inRootSection = false

                    val pathSegments = parsePath(sectionPath)
                    val currentMap = navigateToNestedSection(result, pathSegments)
                    val finalSegment = pathSegments.last()

                    var tableArray = currentMap[finalSegment] as? MutableList<MutableMap<String, Any?>>
                    if (tableArray == null) {
                        tableArray = mutableListOf()
                        currentMap[finalSegment] = tableArray
                    }

                    currentSection = mutableMapOf()
                    tableArray.add(currentSection)
                    continue
                }

                val tableMatcher = TABLE_PATTERN.matcher(trimmedLine)
                if (tableMatcher.matches()) {
                    val sectionPath = tableMatcher.group(1).trim()
                    inRootSection = false

                    val pathSegments = parsePath(sectionPath)
                    val currentMap = navigateToNestedSection(result, pathSegments)
                    val finalSegment = pathSegments.last()

                    currentSection = mutableMapOf()
                    currentMap[finalSegment] = currentSection
                    continue
                }

                val multilineStartMatcher = MULTILINE_STRING_START_PATTERN.matcher(line)
                if (multilineStartMatcher.matches()) {
                    multilineKey = multilineStartMatcher.group(1).trim()
                    val firstLine = multilineStartMatcher.group(2)

                    val endMatcher = MULTILINE_STRING_END_PATTERN.matcher(firstLine)
                    if (endMatcher.matches()) {
                        val value = endMatcher.group(1)
                        assignValueToSection(result, currentSection, inRootSection, multilineKey, value)
                        multilineKey = null
                    } else {
                        multilineValue = StringBuilder(firstLine)
                        if (firstLine.isNotEmpty()) {
                            multilineValue.append('\n')
                        }
                        inMultilineString = true
                    }
                    continue
                }

                val keyValueMatcher = KEY_VALUE_PATTERN.matcher(trimmedLine)
                if (keyValueMatcher.matches()) {
                    val key = keyValueMatcher.group(1).trim()
                    val value = parseValue(keyValueMatcher.group(2).trim())
                    assignValueToSection(result, currentSection, inRootSection, key, value)
                }
            }

            return result
        }

        private fun parseValue(value: String?): Any {
            if (value.isNullOrEmpty()) {
                return ""
            }

            val v = value.trim()
            val length = v.length

            if (length >= 2) {
                val firstChar = v[0]
                val lastChar = v[length - 1]
                if ((firstChar == '"' && lastChar == '"') ||
                    (firstChar == '\'' && lastChar == '\'')
                ) {
                    return v.substring(1, length - 1)
                }
            }

            when (v.lowercase()) {
                "true" -> return true
                "false" -> return false
            }

            try {
                return if (v.contains('.')) {
                    v.toBigDecimal()
                } else {
                    v.toLong()
                }
            } catch (_: NumberFormatException) {
                // Continue to date parsing
            }

            try {
                return when {
                    v.contains('T') -> LocalDateTime.parse(v)
                    v.contains(':') -> LocalTime.parse(v)
                    v.contains('-') -> LocalDate.parse(v)
                    else -> v
                }
            } catch (_: DateTimeParseException) {
            }

            return v
        }

        private fun parsePath(path: String): Array<String> {
            if (++cacheAccessCounter % 100 == 0) {
                checkAndCleanupCache()
            }

            if (cacheAccessCounter % 10 == 0 && pathCache.size > MAX_CACHE_SIZE) {
                forceCleanupIfNeeded()
            }

            return if (pathCache.containsKey(path)) {
                cacheHitCounter++
                pathCache[path]!!
            } else {
                cacheMissCounter++
                pathCache.computeIfAbsent(path) { computeParsePath(it) }
            }
        }

        fun getCacheStats(): Map<String, Any> {
            return mapOf(
                "size" to pathCache.size,
                "maxSize" to MAX_CACHE_SIZE,
                "accessCount" to cacheAccessCounter,
                "hitCount" to cacheHitCounter,
                "missCount" to cacheMissCounter,
                "hitRate" to if (cacheAccessCounter > 0) cacheHitCounter.toDouble() / cacheAccessCounter else 0.0
            )
        }

        fun clearPathCache() {
            pathCache.clear()
            cacheAccessCounter = 0
            cacheHitCounter = 0
            cacheMissCounter = 0
            LOG.info("Path cache cleared")
        }

        private fun checkAndCleanupCache() {
            val currentSize = pathCache.size
            if (currentSize > MAX_CACHE_SIZE * CACHE_CLEANUP_THRESHOLD) {
                cleanupCache()
            }
        }

        private fun cleanupCache() {
            try {
                val targetSize = (MAX_CACHE_SIZE * (1 - CACHE_CLEANUP_RATIO)).toInt()
                val currentSize = pathCache.size

                if (currentSize <= targetSize) return

                val entriesToRemove = currentSize - targetSize
                val allKeys = pathCache.keys().toList()

                val keysToRemove = allKeys
                    .shuffled()
                    .sortedByDescending { it.length }
                    .take(entriesToRemove)

                keysToRemove.forEach { pathCache.remove(it) }

                LOG.info("Path cache cleanup: removed $entriesToRemove entries, remaining: ${pathCache.size}")
            } catch (e: Exception) {
                LOG.warn("Failed to cleanup path cache", e)
            }
        }

        private fun forceCleanupIfNeeded() {
            if (pathCache.size > MAX_CACHE_SIZE) {
                val emergencyTargetSize = MAX_CACHE_SIZE / 2
                val allKeys = pathCache.keys().toList()
                val keysToRemove = allKeys.shuffled().take(pathCache.size - emergencyTargetSize)
                keysToRemove.forEach { pathCache.remove(it) }
                LOG.warn("Emergency cache cleanup: forced removal of ${keysToRemove.size} entries")
            }
        }

        private fun computeParsePath(path: String): Array<String> {
            if (!path.contains(".")) {
                return arrayOf(path)
            }

            val parts = mutableListOf<String>()
            val currentPart = StringBuilder()
            var inQuotes = false
            var quoteChar = '\u0000'

            for (c in path) {
                if (!inQuotes) {
                    when (c) {
                        '"', '\'' -> {
                            inQuotes = true
                            quoteChar = c
                            // Don't add the quote character to the part
                        }

                        '.' -> {
                            if (currentPart.isNotEmpty()) {
                                parts.add(currentPart.toString())
                                currentPart.clear()
                            }
                        }

                        else -> currentPart.append(c)
                    }
                } else {
                    if (c == quoteChar) {
                        inQuotes = false
                        quoteChar = '\u0000'
                        // Don't add the quote character to the part
                    } else {
                        currentPart.append(c)
                    }
                }
            }

            if (currentPart.isNotEmpty()) {
                parts.add(currentPart.toString())
            }

            return parts.toTypedArray()
        }
    }
}