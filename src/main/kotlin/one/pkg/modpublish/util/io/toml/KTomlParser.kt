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
import java.util.regex.Pattern

@Suppress("UNCHECKED_CAST", "UNUSED")
class KTomlParser internal constructor(val parsedData: MutableMap<String, Any?>) : Closeable {
    fun toJson(): String {
        return parsedData.toJson()
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

    fun canAsTomlParser(key: String): Boolean {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            return when (val value = parsedData[key]) {
                is Map<*, *> -> true
                is List<*> -> value.isNotEmpty() && value[0] is Map<*, *>
                else -> false
            }
        }

        if (pathSegments.size > 1) {
            val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
            val parent = navigateToPath(parentPath, 0)
            return parent?.canAsTomlParser(pathSegments.last()) ?: false
        }

        return false
    }

    fun getAsTomlParser(key: String): KTomlParser? {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            return when (val value = parsedData[key]) {
                is MutableMap<*, *> -> KTomlParser(value as MutableMap<String, Any?>)
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val list = value as List<MutableMap<String, Any?>>
                    if (list.isNotEmpty()) KTomlParser(list[0]) else null
                }

                else -> null
            }
        }

        return navigateToPath(pathSegments, 0)
    }

    fun getAsTomlArray(key: String): KTomlArray {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            return when (val value = parsedData[key]) {
                is List<*> -> KTomlArray(value as List<Map<String, Any?>>)
                is Map<*, *> -> KTomlArray(listOf(value as Map<String, Any?>))
                else -> KTomlArray(emptyList())
            }
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.getAsTomlArray(pathSegments.last()) ?: KTomlArray(emptyList())
    }

    fun canAsTomlArray(key: String): Boolean {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            val value = parsedData[key]
            return value is List<*> || value is Map<*, *>
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.canAsTomlArray(pathSegments.last()) ?: false
    }

    fun getAsString(key: String, defaultValue: String = ""): String {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            val value = parsedData[key]
            return value?.toString() ?: defaultValue
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.getAsString(pathSegments.last(), defaultValue) ?: defaultValue
    }

    fun canAsString(key: String): Boolean {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            val value = parsedData[key]
            return value != null && value !is List<*> && value !is Map<*, *>
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.canAsString(pathSegments.last()) ?: false
    }

    fun getAsInt(key: String, defaultValue: Int = 0): Int {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            return when (val value = parsedData[key]) {
                is Number -> value.toInt()
                else -> value?.toString()?.toIntOrNull() ?: defaultValue
            }
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.getAsInt(pathSegments.last(), defaultValue) ?: defaultValue
    }

    fun canAsInt(key: String): Boolean {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            val value = parsedData[key] ?: return false
            return value is Number || value.toString().toIntOrNull() != null
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.canAsInt(pathSegments.last()) ?: false
    }

    fun getAsLong(key: String, defaultValue: Long = 0L): Long {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            return when (val value = parsedData[key]) {
                is Number -> value.toLong()
                else -> value?.toString()?.toLongOrNull() ?: defaultValue
            }
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.getAsLong(pathSegments.last(), defaultValue) ?: defaultValue
    }

    fun canAsLong(key: String): Boolean {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            val value = parsedData[key] ?: return false
            return value is Number || value.toString().toLongOrNull() != null
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.canAsLong(pathSegments.last()) ?: false
    }

    fun getAsDouble(key: String, defaultValue: Double = 0.0): Double {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            return when (val value = parsedData[key]) {
                is Number -> value.toDouble()
                else -> value?.toString()?.toDoubleOrNull() ?: defaultValue
            }
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.getAsDouble(pathSegments.last(), defaultValue) ?: defaultValue
    }

    fun canAsDouble(key: String): Boolean {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            val value = parsedData[key] ?: return false
            return value is Number || value.toString().toDoubleOrNull() != null
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.canAsDouble(pathSegments.last()) ?: false
    }

    fun getAsBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            return when (val value = parsedData[key]) {
                is Boolean -> value
                else -> value?.toString()?.toBooleanStrictOrNull() ?: defaultValue
            }
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.getAsBoolean(pathSegments.last(), defaultValue) ?: defaultValue
    }

    fun canAsBoolean(key: String): Boolean {
        val pathSegments = parsePath(key)

        if (pathSegments.size == 1) {
            val value = parsedData[key] ?: return false
            return value is Boolean ||
                    value.toString().lowercase() == "true" ||
                    value.toString().lowercase() == "false"
        }

        val parentPath = pathSegments.copyOfRange(0, pathSegments.size - 1)
        val parent = navigateToPath(parentPath, 0)
        return parent?.canAsBoolean(pathSegments.last()) ?: false
    }

    @Deprecated("Use getAsTomlParser instead")
    fun getSection(sectionName: String): Map<String, Any?>? {
        return when (val section = parsedData[sectionName]) {
            is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                val list = section as List<Map<String, Any?>>
                if (list.isEmpty()) null else list[0]
            }

            is Map<*, *> -> section as Map<String, Any?>
            else -> null
        }
    }

    @Deprecated("Use getAsTomlArray instead")
    fun getSectionArray(sectionName: String): List<Map<String, Any?>> {
        return when (val section = parsedData[sectionName]) {
            is List<*> -> section as List<Map<String, Any?>>
            is Map<*, *> -> listOf(section as Map<String, Any?>)
            else -> emptyList()
        }
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
     * Get raw parsed data (for advanced usage)
     */
    fun asMap(): Map<String, Any?> {
        return Collections.unmodifiableMap(parsedData)
    }

    /**
     * Get raw parsed data (for advanced usage)
     */
    fun asMutableMap(): MutableMap<String, Any?> {
        return HashMap(parsedData)
    }

    override fun toString(): String {
        return "TomlParser{parsedData=$parsedData}"
    }

    override fun close() {
        parsedData.clear()
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(KTomlParser::class.java)
        private val arrayTablePattern: Pattern = Pattern.compile("^\\s*\\[\\[([^]]+)]]\\s*$")
        private val tablePattern: Pattern = Pattern.compile("^\\s*\\[([^]]+)]\\s*$")
        private val keyValuePattern: Pattern = Pattern.compile("^\\s*([^=]+)\\s*=\\s*(.+)\\s*$")
        private val multilineStringStartPattern: Pattern = Pattern.compile("^\\s*([^=]+)\\s*=\\s*'''(.*)$")
        private val multilineStringEndPattern: Pattern = Pattern.compile("^(.*)'''\\s*$")

        fun empty(): KTomlParser {
            return KTomlParser(HashMap())
        }

        fun fromToml(toml: String): KTomlParser {
            return KTomlParser(parseToml(toml))
        }

        fun fromFile(filePath: Path): KTomlParser {
            return try {
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

        @JvmStatic
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
                    val endMatcher = multilineStringEndPattern.matcher(line)
                    if (endMatcher.matches()) {
                        multilineValue.append(endMatcher.group(1))
                        if (multilineKey != null) {
                            if (currentSection != null) {
                                currentSection[multilineKey] = multilineValue.toString()
                            } else if (inRootSection) {
                                result[multilineKey] = multilineValue.toString()
                            }
                        }
                        inMultilineString = false
                        multilineValue = StringBuilder()
                        multilineKey = null
                    } else {
                        multilineValue.append(line).append('\n')
                    }
                    continue
                }

                val arrayTableMatcher = arrayTablePattern.matcher(trimmedLine)
                if (arrayTableMatcher.matches()) {
                    val sectionPath = arrayTableMatcher.group(1).trim()
                    inRootSection = false

                    val pathSegments = parsePath(sectionPath)
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

                val tableMatcher = tablePattern.matcher(trimmedLine)
                if (tableMatcher.matches()) {
                    val sectionPath = tableMatcher.group(1).trim()
                    inRootSection = false

                    val pathSegments = parsePath(sectionPath)
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

                    val finalSegment = pathSegments.last()
                    currentSection = mutableMapOf()
                    currentMap[finalSegment] = currentSection
                    continue
                }

                val multilineStartMatcher = multilineStringStartPattern.matcher(line)
                if (multilineStartMatcher.matches()) {
                    multilineKey = multilineStartMatcher.group(1).trim()
                    val firstLine = multilineStartMatcher.group(2)

                    val endMatcher = multilineStringEndPattern.matcher(firstLine)
                    if (endMatcher.matches()) {
                        val value = endMatcher.group(1)
                        if (currentSection != null) {
                            currentSection[multilineKey] = value
                        } else if (inRootSection) {
                            result[multilineKey] = value
                        }
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

                val keyValueMatcher = keyValuePattern.matcher(trimmedLine)
                if (keyValueMatcher.matches()) {
                    val key = keyValueMatcher.group(1).trim()
                    val value = parseValue(keyValueMatcher.group(2).trim())
                    if (currentSection != null) {
                        currentSection[key] = value
                    } else if (inRootSection) {
                        result[key] = value
                    }
                }
            }

            return result
        }

        private fun parseValue(value: String?): Any {
            if (value.isNullOrEmpty()) {
                return ""
            }

            val v = value.trim()

            if ((v.startsWith("\"") && v.endsWith("\"")) ||
                (v.startsWith("'") && v.endsWith("'"))
            ) {
                return v.substring(1, v.length - 1)
            }

            if (v.equals("true", ignoreCase = true)) {
                return true
            }
            if (v.equals("false", ignoreCase = true)) {
                return false
            }

            try {
                return if (v.contains(".")) {
                    v.toBigDecimal()
                } else {
                    v.toLong()
                }
            } catch (_: NumberFormatException) {
            }

            try {
                return when {
                    v.contains("T") -> LocalDateTime.parse(v)
                    v.contains(":") -> LocalTime.parse(v)
                    v.contains("-") -> LocalDate.parse(v)
                    else -> v
                }
            } catch (_: DateTimeParseException) {
            }

            return v
        }

        private fun parsePath(path: String): Array<String> {
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

            // Add the last part
            if (currentPart.isNotEmpty()) {
                parts.add(currentPart.toString())
            }

            return parts.toTypedArray()
        }
    }
}