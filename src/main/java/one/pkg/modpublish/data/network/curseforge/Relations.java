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

import java.util.List;

@Data
@SuppressWarnings("unused")
public class Relations {
    /**
     * Project relations list
     */
    @SerializedName("projects")
    private List<ProjectRelation> projects;

    public Relations() {
    }

    public Relations(List<ProjectRelation> projects) {
        this.projects = projects;
    }

    /**
     * Add a project relation
     *
     * @param relation Project relation to add
     */
    public void addProject(ProjectRelation relation) {
        if (projects != null) {
            projects.add(relation);
        }
    }

    /**
     * Check if there are any project relations
     *
     * @return true if there are project relations
     */
    public boolean hasProjects() {
        return projects != null && !projects.isEmpty();
    }

    /**
     * Get number of project relations
     *
     * @return Number of project relations
     */
    public int getProjectCount() {
        return projects != null ? projects.size() : 0;
    }

    @Override
    public String toString() {
        return "Relations{" +
                "projects=" + projects +
                '}';
    }

}