/*
 * Copyright (C) 2025 - 2026 404Setup (https://github.com/404Setup)
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
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.PublishData
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.util.resources.Lang
import one.pkg.modpublish.util.resources.Lang.translate
import java.util.*

abstract class API {
    abstract val id: String

    internal fun HttpRequestBuilder.json() {
        header(HttpHeaders.Accept, "application/json")
        contentType(ContentType.Application.Json)
    }

    internal fun HttpResponse.contentTypeOpt(): Optional<String> {
        return Optional.ofNullable(headers[HttpHeaders.ContentType])
    }

    internal abstract fun createJsonBody(data: PublishData, project: Project): String

    internal suspend fun HttpResponse.statusString(): String? {
        return when (status.value) {
            403, 404, 500 -> "api.common.err.${status.value}".translate()
            302 -> "Duplicate resource"
            400, 401, 422 -> try {
                bodyAsText()
            } catch (_: Exception) {
                "HTTP ${status.value}"
            }

            204 -> null
            else -> validateContentType()
        }
    }

    private fun HttpResponse.validateContentType(): String? {
        val type = contentTypeOpt()
        return if (type.isEmpty || !type.get().contains("application/json")) {
            "api.common.err.format".translate(type.orElse("Unknown"))
        } else {
            null
        }
    }

    abstract suspend fun createVersion(data: PublishData, project: Project): PublishResult

    abstract suspend fun getModInfo(modid: String, project: Project): ModInfo

    open suspend fun patchDescription(modid: String, body: String, project: Project): PublishResult =
        PublishResult.create(this, "Unsupported operation: patchDescription")
}
