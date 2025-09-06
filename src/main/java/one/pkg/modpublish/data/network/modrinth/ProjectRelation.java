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

package one.pkg.modpublish.data.network.modrinth;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import one.pkg.modpublish.data.local.DependencyType;

@Getter
@SuppressWarnings("unused")
public class ProjectRelation {
    @SerializedName("version_id")
    private String versionID;
    @SerializedName("project_id")
    private String projectID;
    @SerializedName("file_name")
    private String fileName;
    @SerializedName("dependency_type")
    private String type;

    public ProjectRelation() {
    }

    public ProjectRelation(String projectID, String type) {
        this.projectID = projectID;
        this.type = type;
    }

    public static ProjectRelation createRequired(String projectID) {
        return new ProjectRelation(projectID, DependencyType.REQUIRED.getModrinthName());
    }

    public static ProjectRelation createOptional(String projectID) {
        return new ProjectRelation(projectID, DependencyType.OPTIONAL.getModrinthName());
    }

    public static ProjectRelation createEmbedded(String projectID) {
        return new ProjectRelation(projectID, DependencyType.EMBEDDED.getModrinthName());
    }

    public static ProjectRelation createIncompatible(String projectID) {
        return new ProjectRelation(projectID, DependencyType.INCOMPATIBLE.getModrinthName());
    }

    public static ProjectRelation create(String projectID, DependencyType type) {
        return switch (type) {
            case EMBEDDED -> createEmbedded(projectID);
            case OPTIONAL -> createOptional(projectID);
            case REQUIRED -> createRequired(projectID);
            case INCOMPATIBLE -> createIncompatible(projectID);
        };
    }

    public boolean isValid() {
        return projectID != null && !projectID.trim().isEmpty() ||
                type != null && !type.trim().isEmpty();
    }
}
