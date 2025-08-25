package one.pkg.modpublish.data.internel;

import one.pkg.modpublish.data.local.MinecraftVersion;

public class MinecraftVersionItem {
    private final MinecraftVersion version;
    private boolean selected;

    public MinecraftVersionItem(MinecraftVersion version, boolean selected) {
        this.version = version;
        this.selected = selected;
    }

    public MinecraftVersion getVersion() {
        return version;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return version.v;
    }
}
