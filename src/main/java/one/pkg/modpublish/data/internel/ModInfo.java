package one.pkg.modpublish.data.internel;

import org.jetbrains.annotations.Nullable;

public record ModInfo(@Nullable String modid,  @Nullable String slug, @Nullable String name, @Nullable String failed) {
    public static ModInfo of(@Nullable String failed) {
        return new ModInfo(null, null, null, failed);
    }

    public static ModInfo[] ofs(@Nullable String failed) {
        return new ModInfo[]{of(failed)};
    }

    public static ModInfo of(String modid, String slug, String name) {
        return new ModInfo(modid, slug, name, null);
    }

    public static ModInfo[] of(ModInfo... modInfos) {
        return modInfos;
    }
}
