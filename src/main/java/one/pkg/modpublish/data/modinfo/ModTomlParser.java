package one.pkg.modpublish.data.modinfo;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public record ModTomlParser(@NotNull Map<String, Map<String, String>> parsedData) implements AutoCloseable {
    private static final com.intellij.openapi.diagnostic.Logger LOG = Logger.getInstance(ModTomlParser.class);

    public static ModTomlParser of(@NotNull Path filePath) {
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            return new ModTomlParser(parseToml(content));
        } catch (Exception e) {
            LOG.error("Failed to read TOML file", e);
            return new ModTomlParser(new HashMap<>());
        }
    }

    public static ModTomlParser of(@NotNull InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            return new ModTomlParser(parseToml(content.toString()));
        } catch (Exception e) {
            LOG.error("Failed to read TOML from InputStream", e);
            return new ModTomlParser(new HashMap<>());
        }
    }

    public static ModTomlParser of(@NotNull File file) {
        return of(file.toPath());
    }

    public static ModTomlParser of(@NotNull String content) {
        return new ModTomlParser(parseToml(content));
    }

    private static Map<String, Map<String, String>> parseToml(String content) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Map<String, String> currentSection = null;
        String currentSectionName;

        Pattern sectionPattern = Pattern.compile("^\\s*\\[\\[?([^]]+)]]?\\s*$");
        Pattern keyValuePattern = Pattern.compile("^\\s*([^=]+)\\s*=\\s*(.+)\\s*$");

        String[] lines = content.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            Matcher sectionMatcher = sectionPattern.matcher(line);
            if (sectionMatcher.matches()) {
                currentSectionName = sectionMatcher.group(1).trim();
                currentSection = new HashMap<>();
                result.put(currentSectionName, currentSection);
                continue;
            }

            Matcher keyValueMatcher = keyValuePattern.matcher(line);
            if (keyValueMatcher.matches() && currentSection != null) {
                String key = keyValueMatcher.group(1).trim();
                String value = keyValueMatcher.group(2).trim();

                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                currentSection.put(key, value);
            }
        }

        return result;
    }


    @SuppressWarnings("all")
    @Nullable
    public LocalModInfo get() {
        try {
            Map<String, String> modsSection = parsedData.get("mods");

            if (modsSection == null) {
                LOG.warn("No mods section found in TOML file");
                return null;
            }

            String displayName = getString(modsSection, "displayName");
            String version = getString(modsSection, "version");

            return new LocalModInfo(displayName, version);
        } catch (Exception e) {
            LOG.error("Failed to parse mod.toml", e);
            return null;
        }
    }

    public String getString(Map<String, String> table, String key) {
        return table.getOrDefault(key, "");
    }


    @Override
    public void close() {
        this.parsedData.clear();
    }
}