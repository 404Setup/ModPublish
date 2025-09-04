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
import lombok.Setter;

@Getter
@SuppressWarnings("unused")
public class SupportedInfo {
    public SupportTarget client;
    public SupportTarget server;

    public SupportedInfo() {
    }

    public SupportedInfo(SupportTarget client, SupportTarget server) {
        this.client = client;
        this.server = server;
    }

    @Getter
    @Setter
    public static class SupportTarget {
        public int cfid;
        public boolean enabled = false;

        public SupportTarget() {
        }

        public SupportTarget(int cfid) {
            this.cfid = cfid;
        }

    }
}