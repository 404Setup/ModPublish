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

package one.pkg.modpublish.data.local;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class LauncherInfo {
    public String n;    // name
    public String id;   // id
    public int cfid;    // curseforge id

    public LauncherInfo() {
    }

    public LauncherInfo(String name, String id, int cfid) {
        this.n = name;
        this.id = id;
        this.cfid = cfid;
    }
}