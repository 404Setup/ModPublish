package one.pkg.modpublish.util.metadata;

import com.intellij.openapi.diagnostic.Logger;
import one.pkg.modpublish.data.internel.LocalModInfo;
import one.pkg.modpublish.util.io.TomlParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

@SuppressWarnings("unused")
public record ModTomlParser(@NotNull TomlParser parser) implements AutoCloseable {
    private static final Logger LOG = Logger.getInstance(ModTomlParser.class);

    public static ModTomlParser of(@NotNull Path filePath) {
        return new ModTomlParser(TomlParser.parseFile(filePath));
    }

    public static ModTomlParser of(@NotNull InputStream inputStream) {
        return new ModTomlParser(TomlParser.parseStream(inputStream));
    }

    public static ModTomlParser of(@NotNull File file) {
        return of(file.toPath());
    }

    public static ModTomlParser of(@NotNull String content) {
        return new ModTomlParser(TomlParser.parse(content));
    }

    @SuppressWarnings("all")
    @Nullable
    public LocalModInfo get() {
        try {
            @Nullable TomlParser mods = parser.getAsTomlParser("mods");
            if (mods == null || mods.isEmpty()) {
                LOG.warn("No mods section found in TOML file");
                return null;
            }

            String displayName = mods.getAsString("displayName");
            String version = mods.getAsString("version");

            return new LocalModInfo(displayName, version, getMinecraftVersions(mods.getAsString("modId")));
        } catch (Exception e) {
            LOG.error("Failed to parse mod.toml", e);
            return null;
        }
    }

    @NotNull
    public String getMinecraftVersions(String modId) {
        var arrays = parser.getAsTomlArray("dependencies.\"" + modId + "\"");
        if (arrays.isEmpty()) arrays = parser.getAsTomlArray("dependencies." + modId);
        if (arrays.isEmpty())
            return "";
        for (TomlParser array : arrays) {
            if (array.getAsString("modId").equals("minecraft"))
                return array.getAsString("versionRange");
        }
        return "";
    }

    @Override
    public void close() {
    }
}