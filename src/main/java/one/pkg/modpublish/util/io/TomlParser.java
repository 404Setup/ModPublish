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

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class TomlParser implements Closeable {
    private static final Logger LOG = Logger.getInstance(TomlParser.class);

    private final Map<String, Object> parsedData;

    private TomlParser(Map<String, Object> parsedData) {
        this.parsedData = parsedData;
    }

    public static TomlParser empty() {
        return new TomlParser(new HashMap<>());
    }

    @NotNull
    public static TomlParser fromToml(@NotNull String toml) {
        return new TomlParser(parseToml(toml));
    }

    @NotNull
    public static TomlParser fromFile(@NotNull Path filePath) {
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            return fromToml(content);
        } catch (Exception e) {
            LOG.error("Failed to read TOML file: " + filePath, e);
            return new TomlParser(new HashMap<>());
        }
    }

    @NotNull
    public static TomlParser fromFile(@NotNull File file) {
        return fromFile(file.toPath());
    }

    @NotNull
    public static TomlParser fromReader(@NotNull Reader reader) {
        try (BufferedReader bufferedReader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append('\n');
            }
            return fromToml(content.toString());
        } catch (Exception e) {
            LOG.error("Failed to read TOML from Reader", e);
            return new TomlParser(new HashMap<>());
        }
    }

    @NotNull
    public static TomlParser fromStream(@NotNull InputStream inputStream) {
        return fromReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private static Map<String, Object> parseToml(String content) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> currentSection = null;
        String currentSectionName = null;
        boolean inRootSection = true;

        Pattern arrayTablePattern = Pattern.compile("^\\s*\\[\\[([^]]+)]]\\s*$");
        Pattern tablePattern = Pattern.compile("^\\s*\\[([^]]+)]\\s*$");
        Pattern keyValuePattern = Pattern.compile("^\\s*([^=]+)\\s*=\\s*(.+)\\s*$");
        Pattern multilineStringStartPattern = Pattern.compile("^\\s*([^=]+)\\s*=\\s*'''(.*)$");
        Pattern multilineStringEndPattern = Pattern.compile("^(.*)'''\\s*$");

        String[] lines = content.split("\\r?\\n");
        boolean inMultilineString = false;
        StringBuilder multilineValue = new StringBuilder();
        String multilineKey = null;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                if (inMultilineString) {
                    multilineValue.append(line).append('\n');
                }
                continue;
            }

            if (inMultilineString) {
                Matcher endMatcher = multilineStringEndPattern.matcher(line);
                if (endMatcher.matches()) {
                    multilineValue.append(endMatcher.group(1));
                    if (currentSection != null && multilineKey != null) {
                        currentSection.put(multilineKey, multilineValue.toString());
                    } else if (inRootSection && multilineKey != null) {
                        result.put(multilineKey, multilineValue.toString());
                    }
                    inMultilineString = false;
                    multilineValue = new StringBuilder();
                    multilineKey = null;
                } else {
                    multilineValue.append(line).append('\n');
                }
                continue;
            }

            Matcher arrayTableMatcher = arrayTablePattern.matcher(trimmedLine);
            if (arrayTableMatcher.matches()) {
                String sectionPath = arrayTableMatcher.group(1).trim();
                inRootSection = false;

                String[] pathSegments = parsePath(sectionPath);

                Map<String, Object> currentMap = result;
                for (int i = 0; i < pathSegments.length - 1; i++) {
                    String segment = pathSegments[i];
                    Object existing = currentMap.get(segment);
                    if (!(existing instanceof Map)) {
                        Map<String, Object> newMap = new HashMap<>();
                        currentMap.put(segment, newMap);
                        currentMap = newMap;
                    } else {
                        currentMap = (Map<String, Object>) existing;
                    }
                }

                String finalSegment = pathSegments[pathSegments.length - 1];
                List<Map<String, Object>> tableArray = (List<Map<String, Object>>) currentMap.get(finalSegment);
                if (tableArray == null) {
                    tableArray = new ArrayList<>();
                    currentMap.put(finalSegment, tableArray);
                }

                currentSection = new HashMap<>();
                tableArray.add(currentSection);
                continue;
            }

            Matcher tableMatcher = tablePattern.matcher(trimmedLine);
            if (tableMatcher.matches()) {
                String sectionPath = tableMatcher.group(1).trim();
                inRootSection = false;

                String[] pathSegments = parsePath(sectionPath);

                Map<String, Object> currentMap = result;
                for (int i = 0; i < pathSegments.length - 1; i++) {
                    String segment = pathSegments[i];
                    Object existing = currentMap.get(segment);
                    if (!(existing instanceof Map)) {
                        Map<String, Object> newMap = new HashMap<>();
                        currentMap.put(segment, newMap);
                        currentMap = newMap;
                    } else {
                        currentMap = (Map<String, Object>) existing;
                    }
                }

                String finalSegment = pathSegments[pathSegments.length - 1];
                currentSection = new HashMap<>();
                currentMap.put(finalSegment, currentSection);
                continue;
            }

            Matcher multilineStartMatcher = multilineStringStartPattern.matcher(line);
            if (multilineStartMatcher.matches()) {
                multilineKey = multilineStartMatcher.group(1).trim();
                String firstLine = multilineStartMatcher.group(2);

                Matcher endMatcher = multilineStringEndPattern.matcher(firstLine);
                if (endMatcher.matches()) {
                    String value = endMatcher.group(1);
                    if (currentSection != null) {
                        currentSection.put(multilineKey, value);
                    } else if (inRootSection) {
                        result.put(multilineKey, value);
                    }
                    multilineKey = null;
                } else {
                    multilineValue = new StringBuilder(firstLine);
                    if (!firstLine.isEmpty()) {
                        multilineValue.append('\n');
                    }
                    inMultilineString = true;
                }
                continue;
            }

            Matcher keyValueMatcher = keyValuePattern.matcher(trimmedLine);
            if (keyValueMatcher.matches()) {
                String key = keyValueMatcher.group(1).trim();
                Object value = parseValue(keyValueMatcher.group(2).trim());

                if (currentSection != null) {
                    currentSection.put(key, value);
                } else if (inRootSection) {
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    private static Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        value = value.trim();

        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        try {
            if (value.contains(".")) {
                return new BigDecimal(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException ignored) {
        }

        try {
            if (value.contains("T")) {
                return LocalDateTime.parse(value);
            } else if (value.contains(":")) {
                return LocalTime.parse(value);
            } else if (value.contains("-")) {
                return LocalDate.parse(value);
            }
        } catch (DateTimeParseException ignored) {
        }

        return value;
    }

    private static String[] parsePath(@NotNull String path) {
        if (!path.contains(".")) {
            return new String[]{path};
        }

        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '\0';

        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);

            if (!inQuotes) {
                if (c == '"' || c == '\'') {
                    inQuotes = true;
                    quoteChar = c;
                    // Don't add the quote character to the part
                } else if (c == '.') {
                    if (!currentPart.isEmpty()) {
                        parts.add(currentPart.toString());
                        currentPart = new StringBuilder();
                    }
                } else {
                    currentPart.append(c);
                }
            } else {
                if (c == quoteChar) {
                    inQuotes = false;
                    quoteChar = '\0';
                    // Don't add the quote character to the part
                } else {
                    currentPart.append(c);
                }
            }
        }

        // Add the last part
        if (!currentPart.isEmpty()) {
            parts.add(currentPart.toString());
        }

        return parts.toArray(new String[0]);
    }

    public String toJson() {
        return JsonParser.toJson(parsedData);
    }

    @Override
    public void close() {
        parsedData.clear();
    }

    /**
     * Get a value by key, returns null if not found
     */
    @Nullable
    public Object get(@NotNull String key) {
        return parsedData.get(key);
    }

    /**
     * Check if a key exists
     */
    public boolean has(@NotNull String key) {
        return parsedData.containsKey(key);
    }

    /**
     * Navigate through nested path using parsed path segments
     */
    @Nullable
    private TomlParser navigateToPath(@NotNull String[] pathSegments, int startIndex) {
        if (startIndex >= pathSegments.length) {
            return this;
        }

        String currentSegment = pathSegments[startIndex];
        Object value = parsedData.get(currentSegment);

        if (value instanceof Map) {
            TomlParser nextLevel = new TomlParser((Map<String, Object>) value);
            return nextLevel.navigateToPath(pathSegments, startIndex + 1);
        } else if (value instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) value;
            if (!list.isEmpty()) {
                TomlParser nextLevel = new TomlParser(list.get(0));
                return nextLevel.navigateToPath(pathSegments, startIndex + 1);
            }
        }

        // If we can't navigate further and there are more segments, return null
        return startIndex == pathSegments.length - 1 ? this : null;
    }

    /**
     * Check if the value for the given key can be converted to TomlParser
     */
    public boolean canAsTomlParser(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value instanceof Map) {
                return true;
            } else if (value instanceof List<?> list) {
                return !list.isEmpty() && list.get(0) instanceof Map;
            }
            return false;
        }

        // For nested paths, navigate to the parent and check the final segment
        if (pathSegments.length > 1) {
            String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
            TomlParser parent = navigateToPath(parentPath, 0);
            if (parent != null) {
                return parent.canAsTomlParser(pathSegments[pathSegments.length - 1]);
            }
        }

        return false;
    }

    /**
     * Get a TOML object for the given key. If the key maps to an array, returns the first element.
     * Similar to JsonObject.getAsJsonObject()
     */
    @Nullable
    public TomlParser getAsTomlParser(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value instanceof Map) {
                return new TomlParser((Map<String, Object>) value);
            } else if (value instanceof List) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) value;
                if (!list.isEmpty()) {
                    return new TomlParser(list.get(0));
                }
            }
            return null;
        }

        // For nested paths, navigate step by step
        return navigateToPath(pathSegments, 0);
    }

    /**
     * Get a TOML array for the given key.
     * Similar to JsonObject.getAsJsonArray()
     */
    @NotNull
    public TomlArray getAsTomlArray(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value instanceof List) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) value;
                return new TomlArray(list);
            } else if (value instanceof Map) {
                return new TomlArray(Collections.singletonList((Map<String, Object>) value));
            }
            return new TomlArray(Collections.emptyList());
        }

        // For nested paths, navigate to parent and get array from final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.getAsTomlArray(pathSegments[pathSegments.length - 1]);
        }

        return new TomlArray(Collections.emptyList());
    }

    /**
     * Check if the value for the given key can be converted to TomlArray
     */
    public boolean canAsTomlArray(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            return value instanceof List || value instanceof Map;
        }

        // For nested paths, navigate to parent and check final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.canAsTomlArray(pathSegments[pathSegments.length - 1]);
        }

        return false;
    }

    /**
     * Get a string value by key
     */
    @NotNull
    public String getAsString(@NotNull String key) {
        return getAsString(key, "");
    }

    /**
     * Get a string value by key with default value
     */
    @NotNull
    public String getAsString(@NotNull String key, @NotNull String defaultValue) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            return value != null ? value.toString() : defaultValue;
        }

        // For nested paths, navigate to parent and get value from final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.getAsString(pathSegments[pathSegments.length - 1], defaultValue);
        }

        return defaultValue;
    }

    /**
     * Check if the value for the given key can be converted to String
     */
    public boolean canAsString(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            return value != null && !(value instanceof List) && !(value instanceof Map);
        }

        // For nested paths, navigate to parent and check final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.canAsString(pathSegments[pathSegments.length - 1]);
        }

        return false;
    }

    /**
     * Get an integer value by key
     */
    public int getAsInt(@NotNull String key) {
        return getAsInt(key, 0);
    }

    /**
     * Get an integer value by key with default value
     */
    public int getAsInt(@NotNull String key, int defaultValue) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            try {
                return Integer.parseInt(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        // For nested paths, navigate to parent and get value from final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.getAsInt(pathSegments[pathSegments.length - 1], defaultValue);
        }

        return defaultValue;
    }

    /**
     * Check if the value for the given key can be converted to int
     */
    public boolean canAsInt(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value == null) return false;
            if (value instanceof Number) return true;

            try {
                Integer.parseInt(value.toString());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        // For nested paths, navigate to parent and check final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.canAsInt(pathSegments[pathSegments.length - 1]);
        }

        return false;
    }

    /**
     * Get a long value by key
     */
    public long getAsLong(@NotNull String key) {
        return getAsLong(key, 0L);
    }

    /**
     * Get a long value by key with default value
     */
    public long getAsLong(@NotNull String key, long defaultValue) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            try {
                return Long.parseLong(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        // For nested paths, navigate to parent and get value from final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.getAsLong(pathSegments[pathSegments.length - 1], defaultValue);
        }

        return defaultValue;
    }

    /**
     * Check if the value for the given key can be converted to long
     */
    public boolean canAsLong(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value == null) return false;
            if (value instanceof Number) return true;

            try {
                Long.parseLong(value.toString());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        // For nested paths, navigate to parent and check final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.canAsLong(pathSegments[pathSegments.length - 1]);
        }

        return false;
    }

    /**
     * Get a double value by key
     */
    public double getAsDouble(@NotNull String key) {
        return getAsDouble(key, 0.0);
    }

    /**
     * Get a double value by key with default value
     */
    public double getAsDouble(@NotNull String key, double defaultValue) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            try {
                return Double.parseDouble(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        // For nested paths, navigate to parent and get value from final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.getAsDouble(pathSegments[pathSegments.length - 1], defaultValue);
        }

        return defaultValue;
    }

    /**
     * Check if the value for the given key can be converted to double
     */
    public boolean canAsDouble(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value == null) return false;
            if (value instanceof Number) return true;

            try {
                Double.parseDouble(value.toString());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        // For nested paths, navigate to parent and check final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.canAsDouble(pathSegments[pathSegments.length - 1]);
        }

        return false;
    }

    /**
     * Get a boolean value by key
     */
    public boolean getAsBoolean(@NotNull String key) {
        return getAsBoolean(key, false);
    }

    /**
     * Get a boolean value by key with default value
     */
    public boolean getAsBoolean(@NotNull String key, boolean defaultValue) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            try {
                return Boolean.parseBoolean(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        // For nested paths, navigate to parent and get value from final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.getAsBoolean(pathSegments[pathSegments.length - 1], defaultValue);
        }

        return defaultValue;
    }

    /**
     * Check if the value for the given key can be converted to boolean
     */
    public boolean canAsBoolean(@NotNull String key) {
        String[] pathSegments = parsePath(key);

        if (pathSegments.length == 1) {
            Object value = parsedData.get(key);
            if (value == null) return false;
            if (value instanceof Boolean) return true;

            String stringValue = value.toString().toLowerCase();
            return "true".equals(stringValue) || "false".equals(stringValue);
        }

        // For nested paths, navigate to parent and check final segment
        String[] parentPath = Arrays.copyOf(pathSegments, pathSegments.length - 1);
        TomlParser parent = navigateToPath(parentPath, 0);
        if (parent != null) {
            return parent.canAsBoolean(pathSegments[pathSegments.length - 1]);
        }

        return false;
    }

    /**
     * @deprecated Use {@link #getAsTomlParser(String)} instead
     */
    @Deprecated
    @Nullable
    public Map<String, Object> getSection(@NotNull String sectionName) {
        Object section = parsedData.get(sectionName);
        if (section instanceof List) {
            List<Map<String, Object>> sectionList = (List<Map<String, Object>>) section;
            return sectionList.isEmpty() ? null : sectionList.get(0);
        } else if (section instanceof Map) {
            return (Map<String, Object>) section;
        }
        return null;
    }

    /**
     * @deprecated Use {@link #getAsTomlArray(String)} instead
     */
    @Deprecated
    @NotNull
    public List<Map<String, Object>> getSectionArray(@NotNull String sectionName) {
        Object section = parsedData.get(sectionName);
        if (section instanceof List) {
            return (List<Map<String, Object>>) section;
        } else if (section instanceof Map) {
            return Collections.singletonList((Map<String, Object>) section);
        }
        return Collections.emptyList();
    }

    /**
     * Get all keys
     */
    @NotNull
    public Set<String> keySet() {
        return parsedData.keySet();
    }

    /**
     * Get the size of this TOML object
     */
    public int size() {
        return parsedData.size();
    }

    /**
     * Check if this TOML object is empty
     */
    public boolean isEmpty() {
        return parsedData.isEmpty();
    }

    /**
     * Get raw parsed data (for advanced usage)
     */
    @NotNull
    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(parsedData);
    }

    @Override
    public String toString() {
        return "TomlParser{parsedData=" + parsedData + '}';
    }

    public static class TomlArray implements Iterable<TomlParser> {
        private final List<Map<String, Object>> elements;

        private TomlArray(List<Map<String, Object>> elements) {
            this.elements = elements;
        }

        public String toJson() {
            return JsonParser.toJson(elements);
        }

        /**
         * Get the size of this array
         */
        public int size() {
            return elements.size();
        }

        /**
         * Check if this array is empty
         */
        public boolean isEmpty() {
            return elements.isEmpty();
        }

        /**
         * Get an element as TomlParser by index
         */
        @Nullable
        public TomlParser get(int index) {
            if (index >= 0 && index < elements.size()) {
                return new TomlParser(elements.get(index));
            }
            return null;
        }

        /**
         * Get all elements as List of TomlParser
         */
        @NotNull
        public List<TomlParser> asList() {
            List<TomlParser> result = new ArrayList<>();
            for (Map<String, Object> element : elements) {
                result.add(new TomlParser(element));
            }
            return result;
        }

        /**
         * Get raw elements (for advanced usage)
         */
        @NotNull
        public List<Map<String, Object>> asMapList() {
            return Collections.unmodifiableList(elements);
        }

        @Override
        public String toString() {
            return "TomlArray{elements=" + elements + '}';
        }

        @Override
        public @NotNull Iterator<TomlParser> iterator() {
            return elements.stream().map(TomlParser::new).iterator();
        }

        @Override
        public void forEach(Consumer<? super TomlParser> action) {
            elements.stream().map(TomlParser::new).forEach(action);
        }

        @Override
        public Spliterator<TomlParser> spliterator() {
            return elements.stream().map(TomlParser::new).spliterator();
        }
    }
}