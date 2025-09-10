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

package one.pkg.modpublish.data.result;

import one.pkg.modpublish.api.API;
import one.pkg.modpublish.util.resources.Lang;
import org.jetbrains.annotations.PropertyKey;

public record PublishResult(String result) implements Result {
    public static final PublishResult EMPTY = new PublishResult("");

    public static PublishResult of(@PropertyKey(resourceBundle = Lang.File) String result) {
        return new PublishResult(Lang.get(result));
    }

    public static PublishResult of(@PropertyKey(resourceBundle = Lang.File) String result, Object... params) {
        return new PublishResult(Lang.get(result, params));
    }

    public static PublishResult create(String result) {
        return new PublishResult(result);
    }

    public static PublishResult create(API api, String result) {
        return new PublishResult("Failed API: "+ api.getID() +"; " + result);
    }

    public static PublishResult empty() {
        return EMPTY;
    }

    public boolean isSuccess() {
        return result == null || result.trim().isEmpty();
    }

    public boolean isFailure() {
        return !isSuccess();
    }
}