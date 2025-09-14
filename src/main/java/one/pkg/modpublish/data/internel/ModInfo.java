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

import org.jetbrains.annotations.Nullable;

public record ModInfo(@Nullable String modid, @Nullable String slug, @Nullable String name, @Nullable String failed) {
    static ModInfo EMPTY = new ModInfo(null, null, null, null);

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

    public static ModInfo empty() {
        return EMPTY;
    }
}
