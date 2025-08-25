package one.pkg.modpublish.data.modinfo;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

@SuppressWarnings("unused")
public record ModTomlParser(@NotNull TomlTable toml) implements AutoCloseable {
    public static ModTomlParser of(@NotNull Path filePath) {
        Validate.notNull(filePath, "filePath cannot be null");
        return new ModTomlParser(JToml.jToml().read(filePath));
    }

    public static ModTomlParser of(@NotNull InputStream in) {
        Validate.notNull(in, "in cannot be null");
        return new ModTomlParser(JToml.jToml().read(in));
    }

    public static ModTomlParser of(@NotNull File file) {
        Validate.notNull(file, "file cannot be null");
        return of(file.toPath());
    }

    @NotNull
    @SuppressWarnings("all")
    public String getVersion() {
        try {
            return toml.get("mods").asTable().get("version").asPrimitive().asString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() {
        this.toml.clear();
    }
}