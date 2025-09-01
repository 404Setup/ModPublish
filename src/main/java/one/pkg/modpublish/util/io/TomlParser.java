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
public class TomlParser implements AutoCloseable {
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

    @NotNull
    public static TomlParser parse(@NotNull String content) {
        return fromToml(content);
    }

    @NotNull
    public static TomlParser parseFile(@NotNull Path filePath) {
        return fromFile(filePath);
    }

    @NotNull
    public static TomlParser parseFile(@NotNull File file) {
        return fromFile(file);
    }

    @NotNull
    public static TomlParser parseStream(@NotNull InputStream inputStream) {
        return fromStream(inputStream);
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
                currentSectionName = arrayTableMatcher.group(1).trim();
                inRootSection = false;

                List<Map<String, Object>> tableArray = (List<Map<String, Object>>) result.get(currentSectionName);
                if (tableArray == null) {
                    tableArray = new ArrayList<>();
                    result.put(currentSectionName, tableArray);
                }

                currentSection = new HashMap<>();
                tableArray.add(currentSection);
                continue;
            }

            Matcher tableMatcher = tablePattern.matcher(trimmedLine);
            if (tableMatcher.matches()) {
                currentSectionName = tableMatcher.group(1).trim();
                inRootSection = false;

                currentSection = new HashMap<>();
                result.put(currentSectionName, currentSection);
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
     * Get a TOML object for the given key. If the key maps to an array, returns the first element.
     * Similar to JsonObject.getAsJsonObject()
     */
    @Nullable
    public TomlParser getAsTomlParser(@NotNull String key) {
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

    /**
     * Get a TOML array for the given key.
     * Similar to JsonObject.getAsJsonArray()
     */
    @NotNull
    public TomlArray getAsTomlArray(@NotNull String key) {
        Object value = parsedData.get(key);
        if (value instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) value;
            return new TomlArray(list);
        } else if (value instanceof Map) {
            return new TomlArray(Collections.singletonList((Map<String, Object>) value));
        }
        return new TomlArray(Collections.emptyList());
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
        Object value = parsedData.get(key);
        return value != null ? value.toString() : defaultValue;
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

    /**
     * @deprecated Use {@link #getAsString(String)} instead
     */
    @Deprecated
    @NotNull
    public String getString(@NotNull String key) {
        return getAsString(key);
    }

    /**
     * @deprecated Use {@link #getAsString(String, String)} instead
     */
    @Deprecated
    @NotNull
    public String getString(@NotNull String key, @NotNull String defaultValue) {
        return getAsString(key, defaultValue);
    }

    /**
     * @deprecated Use {@link #getAsInt(String)} instead
     */
    @Deprecated
    public int getInt(@NotNull String key) {
        return getAsInt(key);
    }

    /**
     * @deprecated Use {@link #getAsInt(String, int)} instead
     */
    @Deprecated
    public int getInt(@NotNull String key, int defaultValue) {
        return getAsInt(key, defaultValue);
    }

    /**
     * @deprecated Use {@link #getAsLong(String)} instead
     */
    @Deprecated
    public long getLong(@NotNull String key) {
        return getAsLong(key);
    }

    /**
     * @deprecated Use {@link #getAsLong(String, long)} instead
     */
    @Deprecated
    public long getLong(@NotNull String key, long defaultValue) {
        return getAsLong(key, defaultValue);
    }

    /**
     * @deprecated Use {@link #getAsBoolean(String)} instead
     */
    @Deprecated
    public boolean getBoolean(@NotNull String key) {
        return getAsBoolean(key);
    }

    /**
     * @deprecated Use {@link #getAsBoolean(String, boolean)} instead
     */
    @Deprecated
    public boolean getBoolean(@NotNull String key, boolean defaultValue) {
        return getAsBoolean(key, defaultValue);
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