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

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class MinecraftVersion {
    // Getter properties for easier access
    @SerializedName("v")
    public String version;  // version
    @SerializedName("t")
    public String type;  // type (release/snapshot)
    @SerializedName("i")
    public int id; // id (curseforge)
    @SerializedName("d")
    public String date;  // date

    public MinecraftVersion() {
    }

    public MinecraftVersion(String version, String type, int id, String date) {
        this.version = version;
        this.type = type;
        this.id = id;
        this.date = date;
    }

    public boolean canReleaseToCurseForge() {
        return type.equals("release") && id > 0;
    }

}