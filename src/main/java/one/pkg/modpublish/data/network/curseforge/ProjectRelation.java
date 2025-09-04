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

package one.pkg.modpublish.data.network.curseforge;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import one.pkg.modpublish.data.local.DependencyType;

@Data
@SuppressWarnings("unused")
public class ProjectRelation {
    /**
     * Project slug identifier
     */
    @SerializedName("slug")
    private String slug;

    /**
     * Project ID - Optional, used for exact project matching
     */
    @SerializedName("projectID")
    private int projectID;

    /**
     * Dependency type
     * Possible values: embeddedLibrary, incompatible, optionalDependency, requiredDependency, tool
     */
    @SerializedName("type")
    private String type;

    public ProjectRelation() {
    }

    public ProjectRelation(String slug, String type) {
        this.slug = slug;
        this.type = type;
    }

    public ProjectRelation(String slug, int projectID, String type) {
        this.slug = slug;
        this.projectID = projectID;
        this.type = type;
    }

    /**
     * Create required dependency relation
     *
     * @param slug Project slug
     * @return ProjectRelation object
     */
    public static ProjectRelation createRequired(String slug) {
        return new ProjectRelation(slug, DependencyType.REQUIRED.getCurseForgeName());
    }

    public static ProjectRelation createRequired(String slug, int projectID) {
        return new ProjectRelation(slug, projectID, DependencyType.REQUIRED.getCurseForgeName());
    }

    /**
     * Create optional dependency relation
     *
     * @param slug Project slug
     * @return ProjectRelation object
     */
    public static ProjectRelation createOptional(String slug) {
        return new ProjectRelation(slug, DependencyType.OPTIONAL.getCurseForgeName());
    }

    public static ProjectRelation createOptional(String slug, int projectID) {
        return new ProjectRelation(slug, projectID, DependencyType.OPTIONAL.getCurseForgeName());
    }

    /**
     * Create embedded library relation
     *
     * @param slug Project slug
     * @return ProjectRelation object
     */
    public static ProjectRelation createEmbedded(String slug) {
        return new ProjectRelation(slug, DependencyType.EMBEDDED.getCurseForgeName());
    }

    public static ProjectRelation createEmbedded(String slug, int projectID) {
        return new ProjectRelation(slug, projectID, DependencyType.EMBEDDED.getCurseForgeName());
    }

    /**
     * Create incompatible relation
     *
     * @param slug Project slug
     * @return ProjectRelation object
     */
    public static ProjectRelation createIncompatible(String slug) {
        return new ProjectRelation(slug, DependencyType.INCOMPATIBLE.getCurseForgeName());
    }

    public static ProjectRelation createIncompatible(String slug, int projectID) {
        return new ProjectRelation(slug, projectID, DependencyType.INCOMPATIBLE.getCurseForgeName());
    }

    public static ProjectRelation create(String slug, DependencyType type) {
        return switch (type) {
            case EMBEDDED -> createEmbedded(slug);
            case OPTIONAL -> createOptional(slug);
            case REQUIRED -> createRequired(slug);
            case INCOMPATIBLE -> createIncompatible(slug);
        };
    }

    public static ProjectRelation create(String slug, int projectID, DependencyType type) {
        return switch (type) {
            case EMBEDDED -> createEmbedded(slug, projectID);
            case OPTIONAL -> createOptional(slug, projectID);
            case REQUIRED -> createRequired(slug, projectID);
            case INCOMPATIBLE -> createIncompatible(slug, projectID);
        };
    }

    /**
     * Check if project ID exists
     *
     * @return true if project ID exists
     */
    public boolean hasProjectID() {
        return projectID > 1;
    }

    /**
     * Validate if fields are valid
     *
     * @return true if required fields are set
     */
    public boolean isValid() {
        return slug != null && !slug.trim().isEmpty() &&
                hasProjectID() ||
                type != null && !type.trim().isEmpty();
    }
}