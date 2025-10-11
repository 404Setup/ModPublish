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
package one.pkg.modpublish.data.internal

data class ModInfo(val modid: String?, val slug: String?, val name: String?, val failed: String?) {
    companion object {
        var EMPTY: ModInfo = ModInfo(null, null, null, null)

        fun of(failed: String?): ModInfo {
            return ModInfo(null, null, null, failed)
        }

        fun of(modid: String?, slug: String?, name: String?): ModInfo {
            return ModInfo(modid, slug, name, null)
        }

        fun of(vararg modInfos: ModInfo): Array<ModInfo> {
            return arrayOf(*modInfos)
        }

        fun empty(): ModInfo {
            return EMPTY
        }
    }
}
