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
package one.pkg.modpublish.data.result

import one.pkg.modpublish.api.API
import one.pkg.modpublish.util.resources.Lang
import org.jetbrains.annotations.PropertyKey

@JvmRecord
data class PublishResult(val result: String?, val id: String) : Result {
    override val isSuccess: Boolean
        get() = result == null || result.trim { it <= ' ' }.isEmpty()

    override val isFailure: Boolean
        get() = !isSuccess

    companion object {
        val EMPTY: PublishResult = PublishResult("", "")

        fun of(@PropertyKey(resourceBundle = Lang.FILE) result: String): PublishResult {
            return PublishResult(Lang.get(result), "")
        }

        fun of(@PropertyKey(resourceBundle = Lang.FILE) result: String, vararg params: Any): PublishResult {
            return PublishResult(Lang.get(result, *params), "")
        }

        fun create(result: String?): PublishResult {
            return PublishResult(result, "")
        }

        fun create(api: API, result: String?): PublishResult {
            return PublishResult(result, api.id)
        }
    }
}