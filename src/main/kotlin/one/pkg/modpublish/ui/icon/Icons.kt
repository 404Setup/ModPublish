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
package one.pkg.modpublish.ui.icon

import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.ui.AnimatedIcon
import javax.swing.Icon

object Icons {
    fun getIcon(path: String): Icon {
        return getIcon(path, Icons::class.java)
    }

    object Target {
        val Github: Icon = getIcon("/icons/github.svg")
        val Gitlab: Icon = getIcon("/icons/gitlab.svg")
        val CurseForge: Icon = getIcon("/icons/curseforge.svg")
        val Modrinth: Icon = getIcon("/icons/modrinth.svg")
        val ReposiliteDark: Icon = getIcon("/icons/repoilite-dark.svg")
        val ReposiliteLight: Icon = getIcon("/icons/repoilite-light.svg")
    }

    object Static {
        val Book: Icon = getIcon("/icons/book.svg")
        val Clipboard: Icon = getIcon("/icons/clipboard.svg")
        val Library: Icon = getIcon("/icons/library.svg")
        val DataBar: Icon = getIcon("/icons/databar.svg")
        val ListBar: Icon = getIcon("/icons/list-bar.svg")
        val Globe: Icon = getIcon("/icons/globe.svg")
        val Sync: Icon = getIcon("/icons/sync.svg")
        val Success: Icon = getIcon("/icons/checkmark-circle.svg")
        val Failed: Icon = getIcon("/icons/dismiss-circle.svg")
        val Warning: Icon = getIcon("/icons/error-circle.svg")
        val Send: Icon = getIcon("/icons/send.svg")
        val WrenchScrewdriver: Icon = getIcon("/icons/wrench-screwdriver.svg")
        val UncheckedCheckBox: Icon = getIcon("/icons/checkbox-unchecked.svg")
        val CheckedCheckBox: Icon = getIcon("/icons/checkbox-checked.svg")
        val DisabledCheckBox: Icon = getIcon("/icons/checkbox-indeterminate.svg")
        val DisabledSelectedCheckBox: Icon = getIcon("/icons/checkbox-warning.svg")
    }

    object Animated {
        val Dashes: Icon = createDashesIcon()

        private fun createDashesIcon(): Icon {
            val frames = 60
            val icons = arrayOfNulls<Icon>(frames)

            for (i in 0..<frames) {
                icons[i] = getIcon("/icons/arrow-clockwise-dashes/frame_$i.svg")
            }
            return AnimatedIcon(33, *icons)
        }
    }
}
