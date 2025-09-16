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

package one.pkg.modpublish.ui.icon;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.AnimatedIcon;

import javax.swing.*;

public class Icons {
    public static Icon getIcon(String path) {
        return IconLoader.getIcon(path, Icons.class);
    }

    public static class Target {
        public static final Icon Github = getIcon("/icons/github.svg");
        public static final Icon CurseForge = getIcon("/icons/curseforge.svg");
        public static final Icon Modrinth = getIcon("/icons/modrinth.svg");
    }

    public static class Static {
        public static final Icon Book = getIcon("/icons/book.svg");
        public static final Icon Clipboard = getIcon("/icons/clipboard.svg");
        public static final Icon Library = getIcon("/icons/library.svg");
        public static final Icon DataBar = getIcon("/icons/databar.svg");
        public static final Icon ListBar = getIcon("/icons/list-bar.svg");
        public static final Icon Globe = getIcon("/icons/globe.svg");
        public static final Icon Success = getIcon("/icons/checkmark-circle.svg");
        public static final Icon Failed = getIcon("/icons/dismiss-circle.svg");
        public static final Icon Warning = getIcon("/icons/error-circle.svg");
        public static final Icon Send = getIcon("/icons/send.svg");
        public static final Icon UncheckedCheckBox = getIcon("/icons/checkbox-unchecked.svg");
        public static final Icon CheckedCheckBox = getIcon("/icons/checkbox-checked.svg");
        public static final Icon DisabledCheckBox = getIcon("/icons/checkbox-indeterminate.svg");
        public static final Icon DisabledSelectedCheckBox = getIcon("/icons/checkbox-warning.svg");
    }

    public static class Animated {
        public static final Icon Dashes = createDashesIcon();

        private static Icon createDashesIcon() {
            Icon[] frames = new Icon[12];
            for (int i = 0; i < 12; i++) {
                frames[i] = getIcon("/icons/arrow-clockwise-dashes/frame_" + i + ".svg");
            }
            return new AnimatedIcon(50, frames);
        }
    }
}
