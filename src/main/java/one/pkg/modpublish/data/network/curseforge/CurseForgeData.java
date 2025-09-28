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
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Tolerate;
import one.pkg.modpublish.data.internal.ReleaseChannel;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.util.io.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Getter
@Builder(toBuilder = true)
public class CurseForgeData {

    /**
     * Changelog - A string describing changes, can be HTML or Markdown format
     */
    @SerializedName("changelog")
    private String changelog;
    /**
     * Changelog type - Optional, defaults to text
     * Valid values: text, html, markdown
     */
    @SerializedName("changelogType")
    private String changelogType;
    /**
     * Display name - Optional, friendly name shown on website
     */
    @SerializedName("displayName")
    private String displayName;
    /**
     * Parent file ID - Optional, parent file of this file
     */
    @SerializedName("parentFileID")
    private Integer parentFileID;
    /**
     * List of supported game versions
     * Not supported if parentFileID is provided
     */
    @SerializedName("gameVersions")
    private List<Integer> gameVersions;
    /**
     * Release type: one of alpha, beta, release
     */
    @SerializedName("releaseType")
    private String releaseType;
    /**
     * Whether marked for manual release - Optional
     * If true, file won't publish immediately after approval, can choose when to publish
     */
    @SerializedName("isMarkedForManualRelease")
    private Boolean isMarkedForManualRelease;
    /**
     * Project relations - Optional
     * Array of project relationships by slug and dependency type
     */
    @SerializedName("relations")
    private Relations relations;

    /**
     * Checks if there is a parent file
     *
     * @return true if there is a parent file
     */
    public boolean parentFile() {
        return parentFileID != null;
    }

    /**
     * Validates that required fields are set
     *
     * @return true if all required fields are set
     */
    public boolean isValid() {
        return changelog != null && !changelog.trim().isEmpty() &&
                releaseType != null && !releaseType.trim().isEmpty() &&
                (parentFile() || (gameVersions != null && !gameVersions.isEmpty()));
    }

    public String toJson() {
        return JsonParser.toJson(this);
    }

    public static class CurseForgeDataBuilder {
        @NotNull
        public CurseForgeDataBuilder changelog(@NotNull String changelog) {
            this.changelog = changelog;
            this.changelogType = "text";
            return this;
        }

        /**
         * Sets HTML format changelog
         *
         * @param htmlChangelog Changelog in HTML format
         */
        @NotNull
        public CurseForgeDataBuilder htmlChangelog(@NotNull String htmlChangelog) {
            this.changelog = htmlChangelog;
            this.changelogType = "html";
            return this;
        }

        /**
         * Sets Markdown format changelog
         *
         * @param markdownChangelog Changelog in Markdown format
         */
        @NotNull
        public CurseForgeDataBuilder markdownChangelog(@NotNull String markdownChangelog) {
            this.changelog = markdownChangelog;
            this.changelogType = "markdown";
            return this;
        }

        @NotNull
        public CurseForgeDataBuilder gameVersion(@NotNull MinecraftVersion version) {
            return version.canReleaseToCurseForge() ? gameVersion(version.getId()) : this;
        }

        @NotNull
        public CurseForgeDataBuilder gameVersion(@NotNull Integer gameVersion) {
            if (this.gameVersions == null) this.gameVersions = new ArrayList<>();
            if (!this.gameVersions.isEmpty() && this.gameVersions.contains(gameVersion)) return this;
            this.gameVersions.add(gameVersion);
            return this;
        }

        @NotNull
        public CurseForgeDataBuilder alpha() {
            return releaseType(ReleaseChannel.Alpha);
        }

        @NotNull
        public CurseForgeDataBuilder beta() {
            return releaseType(ReleaseChannel.Beta);
        }

        @NotNull
        public CurseForgeDataBuilder release() {
            return releaseType(ReleaseChannel.Release);
        }

        @Tolerate
        @NotNull
        public CurseForgeDataBuilder releaseType(@NotNull ReleaseChannel releaseChannel) {
            this.releaseType = releaseChannel.getType();
            return this;
        }

        @NotNull
        public CurseForgeDataBuilder dependency(@NotNull ProjectRelation relation) {
            if (this.relations == null) this.relations = new Relations();
            if (this.relations.getProjects() == null) this.relations.setProjects(new ArrayList<>());
            this.relations.addProject(relation);
            return this;
        }

        @NotNull
        public CurseForgeDataBuilder requiredDependency(@NotNull String slug) {
            return dependency(ProjectRelation.Companion.createRequired(slug));
        }

        @NotNull
        public CurseForgeDataBuilder requiredDependency(@NotNull String slug, int projectID) {
            return dependency(ProjectRelation.Companion.createRequired(slug, projectID));
        }

        @NotNull
        public CurseForgeDataBuilder optionalDependency(@NotNull String slug) {
            return dependency(ProjectRelation.Companion.createOptional(slug));
        }

        @NotNull
        public CurseForgeDataBuilder optionalDependency(@NotNull String slug, int projectID) {
            return dependency(ProjectRelation.Companion.createOptional(slug, projectID));
        }

        @NotNull
        public CurseForgeDataBuilder embeddedLibrary(@NotNull String slug) {
            return dependency(ProjectRelation.Companion.createEmbedded(slug));
        }

        @NotNull
        public CurseForgeDataBuilder embeddedLibrary(@NotNull String slug, int projectID) {
            return dependency(ProjectRelation.Companion.createEmbedded(slug, projectID));
        }

        @NotNull
        public CurseForgeDataBuilder incompatible(@NotNull String slug) {
            return dependency(ProjectRelation.Companion.createIncompatible(slug));
        }

        @NotNull
        public CurseForgeDataBuilder incompatible(@NotNull String slug, int projectID) {
            return dependency(ProjectRelation.Companion.createIncompatible(slug, projectID));
        }
    }
}