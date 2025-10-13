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
import one.pkg.modpublish.data.internal.ModInfo
import one.pkg.modpublish.data.internal.PublishData
import one.pkg.modpublish.data.result.PublishResult
import one.pkg.modpublish.util.resources.Lang

class EmptyAPI : API() {
    override val id: String = Lang.get("failed.10")

    override fun createJsonBody(data: PublishData, project: Project): String = "{}"

    override fun createVersion(data: PublishData, project: Project): PublishResult = PublishResult.create(this, id)

    override fun getModInfo(modid: String, project: Project): ModInfo = ModInfo.EMPTY
}