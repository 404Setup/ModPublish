package one.pkg.modpublish.data.internel;

import lombok.Getter;
import lombok.Setter;
import one.pkg.modpublish.data.local.MinecraftVersion;

@Getter
@Setter
public class MinecraftVersionItem {
    private final MinecraftVersion version;
    private boolean selected;

    public MinecraftVersionItem(MinecraftVersion version, boolean selected) {
        this.version = version;
        this.selected = selected;
    }

    @Override
    public String toString() {
        return version.version;
    }
}
