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
package one.pkg.modpublish.util.resources

import com.intellij.DynamicBundle
import one.pkg.modpublish.util.resources.Lang.FILE
import org.jetbrains.annotations.PropertyKey

object Lang : DynamicBundle(FILE) {
    const val FILE = "messages.ModPublish"

    @JvmStatic
    fun get(@PropertyKey(resourceBundle = FILE) key: String): String {
        return getMessage(key)
    }

    @JvmStatic
    fun get(@PropertyKey(resourceBundle = FILE) key: String, vararg params: Any): String {
        return getMessage(key, *params)
    }
}
