package one.pkg.modpublish.data.internel;

import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.local.SupportedInfo;

import java.io.File;
import java.util.List;

public record PublishData(String versionName, String versionNumber, boolean github, boolean gitlab, boolean modrinth,
                          boolean modrinthTest, boolean curseforge, List<LauncherInfo> loaders, SupportedInfo supportedInfo,
                          List<MinecraftVersion> minecraftVersions, String changelog, List<DependencyInfo> dependencies,
                          File file) {
}