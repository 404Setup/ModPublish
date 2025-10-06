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

    abstract fun getAB(): Boolean
    abstract fun updateAB()

    fun getJsonRequest(builder: Request.Builder): Request.Builder {
        return builder.header("Accept", "application/json")
    }

    fun getFormRequest(builder: Request.Builder): Request.Builder {
        return builder.header("Content-Type", "multipart/form-data")
    }

    fun getContentType(response: Response): Optional<String> {
        return Optional.ofNullable<String>(response.header("Content-Type"))
    }

    abstract fun createJsonBody(data: PublishData, project: Project): String

    fun getStatus(response: Response): String? {
        if (response.code == 403) return Lang.get("api.common.err.403")
        if (response.code == 404) return Lang.get("api.common.err.404")
        if (response.code == 500) return Lang.get("api.common.err.500")
        if (response.code == 302) return "Duplicate resource"
        try {
            if (response.code == 400 || response.code == 401 || response.code == 422) return response.body.string()
        } catch (_: Exception) {
            return "HTTP " + response.code
        }
        val ct = getContentType(response)
        if (ct.isEmpty || !ct.get().contains("application/json")) return Lang.get(
            "api.common.err.format",
            ct.orElse("Unknown")
        )
        return null
    }

    abstract fun createVersion(data: PublishData, project: Project): PublishResult

    abstract fun getModInfo(modid: String, project: Project): ModInfo

    companion object {
        val baseRequestBuilder: Request.Builder
            get() = Request.Builder().header("User-Agent", "modpublish/v1 (github.com/404Setup/ModPublish)")
    }
}
