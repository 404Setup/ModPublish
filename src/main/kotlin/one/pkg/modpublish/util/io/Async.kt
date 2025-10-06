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
package one.pkg.modpublish.util.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

object Async {
    inline fun <T> rAsync(crossinline block: suspend () -> T): Deferred<T> {
        return Dispatchers.Default.rAsync {
            block()
        }
    }

    inline fun <T> CoroutineContext.rAsync(crossinline block: suspend () -> T): Deferred<T> {
        return CoroutineScope(this).async {
            block()
        }
    }

    inline fun async(crossinline callable: () -> Unit) {
        Dispatchers.Default.async { callable() }
    }

    inline fun CoroutineContext.async(crossinline callable: () -> Unit) {
        CoroutineScope(this).async {
            callable()
        }
    }
}
