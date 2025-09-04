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

package one.pkg.modpublish.util.resources;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

@NonNls
public class Lang extends DynamicBundle {
    public static final String File = "messages.ModPublish";
    private static final Lang INSTANCE = new Lang();

    private Lang() {
        super(File);
    }

    public static String get(@PropertyKey(resourceBundle = File) String key) {
        return INSTANCE.getMessage(key);
    }

    public static String get(@PropertyKey(resourceBundle = File) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}