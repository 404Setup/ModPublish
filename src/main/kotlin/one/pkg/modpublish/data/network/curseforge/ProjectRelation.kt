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
package one.pkg.modpublish.data.network.curseforge

import com.google.gson.annotations.SerializedName
import one.pkg.modpublish.data.local.DependencyType

@Suppress("UNUSED")
data class ProjectRelation(
    /**
     * Project slug identifier
     */
    @SerializedName("slug")
    var slug: String? = null,

    /**
     * Project ID - Optional, used for exact project matching
     */
    @SerializedName("projectID")
    var projectID: Int = 0,

    /**
     * Dependency type
     * Possible values: embeddedLibrary, incompatible, optionalDependency, requiredDependency, tool
     */
    @SerializedName("type")
    var type: String? = null
) {
    constructor() : this(null, 0, null)

    constructor(slug: String, type: String) : this(slug, 0, type)

    /**
     * Check if project ID exists
     *
     * @return true if project ID exists
     */
    fun hasProjectID(): Boolean {
        return projectID > 1
    }

    companion object {
        /**
         * Create required dependency relation
         *
         * @param slug Project slug
         * @return ProjectRelation object
         */
        fun required(slug: String): ProjectRelation {
            return ProjectRelation(slug, DependencyType.REQUIRED.curseForgeName)
        }

        fun required(slug: String, projectID: Int): ProjectRelation {
            return ProjectRelation(slug, projectID, DependencyType.REQUIRED.curseForgeName)
        }

        /**
         * Create optional dependency relation
         *
         * @param slug Project slug
         * @return ProjectRelation object
         */
        fun optional(slug: String): ProjectRelation {
            return ProjectRelation(slug, DependencyType.OPTIONAL.curseForgeName)
        }

        fun optional(slug: String, projectID: Int): ProjectRelation {
            return ProjectRelation(slug, projectID, DependencyType.OPTIONAL.curseForgeName)
        }

        /**
         * Create embedded library relation
         *
         * @param slug Project slug
         * @return ProjectRelation object
         */
        fun embedded(slug: String): ProjectRelation {
            return ProjectRelation(slug, DependencyType.EMBEDDED.curseForgeName)
        }

        fun embedded(slug: String, projectID: Int): ProjectRelation {
            return ProjectRelation(slug, projectID, DependencyType.EMBEDDED.curseForgeName)
        }

        /**
         * Create incompatible relation
         *
         * @param slug Project slug
         * @return ProjectRelation object
         */
        fun incompatible(slug: String): ProjectRelation {
            return ProjectRelation(slug, DependencyType.INCOMPATIBLE.curseForgeName)
        }

        fun incompatible(slug: String, projectID: Int): ProjectRelation {
            return ProjectRelation(slug, projectID, DependencyType.INCOMPATIBLE.curseForgeName)
        }

        fun create(slug: String, type: DependencyType): ProjectRelation {
            return when (type) {
                DependencyType.EMBEDDED -> embedded(slug)
                DependencyType.OPTIONAL -> optional(slug)
                DependencyType.REQUIRED -> required(slug)
                DependencyType.INCOMPATIBLE -> incompatible(slug)
            }
        }

        fun create(slug: String, projectID: Int, type: DependencyType): ProjectRelation {
            return when (type) {
                DependencyType.EMBEDDED -> embedded(slug, projectID)
                DependencyType.OPTIONAL -> optional(slug, projectID)
                DependencyType.REQUIRED -> required(slug, projectID)
                DependencyType.INCOMPATIBLE -> incompatible(slug, projectID)
            }
        }
    }
}