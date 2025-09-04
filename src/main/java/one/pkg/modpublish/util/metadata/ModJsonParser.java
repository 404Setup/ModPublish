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

package one.pkg.modpublish.util.metadata;

import com.google.gson.JsonObject;
import one.pkg.modpublish.data.internel.LocalModInfo;
import one.pkg.modpublish.util.io.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ModJsonParser {
    private final JsonObject json;

    public ModJsonParser(InputStream inputStream) {
        Reader reader = new InputStreamReader(inputStream);

        json = JsonParser.getJsonObject(reader);
    }

    @Nullable
    public LocalModInfo get() {
        try {
            return new LocalModInfo(json.get("name").getAsString(), json.get("version").getAsString(),
                    json.get("depends").getAsJsonObject().get("minecraft").getAsString());
        } catch (Exception e) {
            return null;
        }
    }
}
