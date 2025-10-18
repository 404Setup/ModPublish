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
        fun createRequired(projectID: String): ProjectRelation {
            return ProjectRelation(projectID, DependencyType.REQUIRED)
        }

        fun createOptional(projectID: String): ProjectRelation {
            return ProjectRelation(projectID, DependencyType.OPTIONAL)
        }

        fun createEmbedded(projectID: String): ProjectRelation {
            return ProjectRelation(projectID, DependencyType.EMBEDDED)
        }

        fun createIncompatible(projectID: String): ProjectRelation {
            return ProjectRelation(projectID, DependencyType.INCOMPATIBLE)
        }

        fun create(projectID: String, type: DependencyType): ProjectRelation {
            return when (type) {
                DependencyType.EMBEDDED -> createEmbedded(projectID)
                DependencyType.OPTIONAL -> createOptional(projectID)
                DependencyType.REQUIRED -> createRequired(projectID)
                DependencyType.INCOMPATIBLE -> createIncompatible(projectID)
            }
        }
    }
}
