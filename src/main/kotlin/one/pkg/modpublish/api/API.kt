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
package one.pkg.modpublish.api

import com.intellij.openapi.project.Project
import okhttp3.Request
import okhttp3.Response
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.PublishData
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.util.resources.Lang
import java.util.*

abstract class API {
    abstract val id: String

    internal fun Request.Builder.json(): Request.Builder {
        return header("Accept", "application/json")
    }

    internal fun Request.Builder.form(): Request.Builder {
        return header("Content-Type", "multipart/form-data")
    }

    internal fun Response.contentType(): Optional<String> {
        return Optional.ofNullable<String>(header("Content-Type"))
    }

    internal abstract fun createJsonBody(data: PublishData, project: Project): String

    internal fun Response.status(): String? {
        return when (code) {
            403, 404, 500 -> Lang.get("api.common.err.$code")
            302 -> "Duplicate resource"
            400, 401, 422 -> try {
                body.string()
            } catch (_: Exception) {
                "HTTP $code"
            }

            204 -> null
            else -> validateContentType()
        }
    }

    private fun Response.validateContentType(): String? {
        val type = contentType()
        return if (type.isEmpty || !type.get().contains("application/json")) {
            Lang.get("api.common.err.format", type.orElse("Unknown"))
        } else {
            null
        }
    }

    abstract fun createVersion(data: PublishData, project: Project): PublishResult

    abstract fun getModInfo(modid: String, project: Project): ModInfo

    open fun patchDescription(modid: String, body: String, project: Project): PublishResult =
        PublishResult.create(this, "Unsupported operation: patchDescription")

    companion object {
        internal val baseRequestBuilder: Request.Builder
            get() = Request.Builder().header("User-Agent", "modpublish/v1 (github.com/404Setup/ModPublish)")
    }
}
