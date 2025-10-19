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
package one.pkg.modpublish.data.network.modrinth

import com.google.gson.annotations.SerializedName
import one.pkg.modpublish.data.local.DependencyType

@Suppress("UNUSED")
data class ProjectRelation(
    @SerializedName("version_id")
    val versionID: String? = null,

    @SerializedName("project_id")
    val projectID: String,

    @SerializedName("file_name")
    val fileName: String? = null,

    @SerializedName("dependency_type")
    val type: String,
) {
    constructor(projectID: String, type: String) : this(null, projectID, null, type)

    constructor(projectID: String, type: DependencyType) : this(projectID, type.modrinthName)

    companion object {
        fun required(projectID: String): ProjectRelation {
            return ProjectRelation(projectID, DependencyType.REQUIRED)
        }

        fun optional(projectID: String): ProjectRelation {
            return ProjectRelation(projectID, DependencyType.OPTIONAL)
        }

        fun embedded(projectID: String): ProjectRelation {
            return ProjectRelation(projectID, DependencyType.EMBEDDED)
        }

        fun incompatible(projectID: String): ProjectRelation {
            return ProjectRelation(projectID, DependencyType.INCOMPATIBLE)
        }

        fun create(projectID: String, type: DependencyType): ProjectRelation {
            return when (type) {
                DependencyType.EMBEDDED -> embedded(projectID)
                DependencyType.OPTIONAL -> optional(projectID)
                DependencyType.REQUIRED -> required(projectID)
                DependencyType.INCOMPATIBLE -> incompatible(projectID)
            }
        }
    }
}
