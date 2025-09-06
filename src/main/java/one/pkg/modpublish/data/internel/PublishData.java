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
                          List<MinecraftVersion> minecraftVersions, String changelog,
                          List<DependencyInfo> dependencies, List<File> files) {
    public record Enabled(boolean github, boolean modrinth, boolean modrinthTest, boolean curseforge) {
        public static Enabled getInstance(boolean[] publishTargets) {
            return new Enabled(publishTargets[0], publishTargets[1], publishTargets[2],
                    publishTargets[3]);
        }

        public static Enabled getInstance(JBCheckBox... jbCheckBoxes) {
            if (jbCheckBoxes.length != 4) throw new IllegalArgumentException("jbCheckBoxes length must be 4");
            return new Enabled(jbCheckBoxes[0].isSelected(), jbCheckBoxes[1].isSelected(),
                    jbCheckBoxes[2].isSelected(), jbCheckBoxes[3].isSelected());
        }
    }
}