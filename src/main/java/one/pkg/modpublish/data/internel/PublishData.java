package one.pkg.modpublish.data.internel;

import com.intellij.ui.components.JBCheckBox;
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.local.SupportedInfo;

import java.io.File;
import java.util.List;

public record PublishData(String versionName, String versionNumber, Enabled enabled, ReleaseChannel releaseChannel,
                          List<LauncherInfo> loaders, SupportedInfo supportedInfo,
                          List<MinecraftVersion> minecraftVersions,
                          String changelog, List<DependencyInfo> dependencies, File file) {
    public record Enabled(boolean github, boolean gitlab, boolean modrinth, boolean modrinthTest, boolean curseforge) {
        public static Enabled getInstance(boolean[] publishTargets) {
            return new Enabled(publishTargets[0], publishTargets[1], publishTargets[2], publishTargets[3], publishTargets[4]);
        }

        public static Enabled getInstance(JBCheckBox... jbCheckBoxes) {
            if (jbCheckBoxes.length != 5) throw new IllegalArgumentException("jbCheckBoxes length must be 5");
            return new Enabled(jbCheckBoxes[0].isSelected(), jbCheckBoxes[1].isSelected(), jbCheckBoxes[2].isSelected(), jbCheckBoxes[3].isSelected(), jbCheckBoxes[4].isSelected());
        }
    }
}