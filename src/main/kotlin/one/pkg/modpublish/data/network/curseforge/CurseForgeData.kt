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
import one.pkg.modpublish.data.internal.ReleaseChannel
import one.pkg.modpublish.data.local.MinecraftVersion
import one.pkg.modpublish.data.network.curseforge.ProjectRelation.Companion.embedded
import one.pkg.modpublish.data.network.curseforge.ProjectRelation.Companion.optional
import one.pkg.modpublish.data.network.curseforge.ProjectRelation.Companion.required

@Suppress("UNUSED")
data class CurseForgeData(
    /**
     * Changelog - A string describing changes, can be HTML or Markdown format
     */
    @SerializedName("changelog")
    var changelog: String? = null,

    /**
     * Changelog type - Optional, defaults to text
     * Valid values: text, html, markdown
     */
    @SerializedName("changelogType")
    var changelogType: String? = null,

    /**
     * Display name - Optional, friendly name shown on website
     */
    @SerializedName("displayName")
    var displayName: String? = null,

    /**
     * Parent file ID - Optional, parent file of this file
     */
    @SerializedName("parentFileID")
    var parentFileID: Int? = null,

    /**
     * List of supported game versions
     * Not supported if parentFileID is provided
     */
    @SerializedName("gameVersions")
    var gameVersions: MutableList<Int>? = null,

    /**
     * Release type: one of alpha, beta, release
     */
    @SerializedName("releaseType")
    var releaseType: String? = null,

    /**
     * Whether marked for manual release - Optional
     * If true, file won't publish immediately after approval, can choose when to publish
     */
    @SerializedName("isMarkedForManualRelease")
    var isMarkedForManualRelease: Boolean? = null,

    /**
     * Project relations - Optional
     * Array of project relationships by slug and dependency type
     */
    @SerializedName("relations")
    var relations: Relations? = null
) {
    constructor(): this(null)

    /**
     * Checks if there is a parent file
     *
     * @return true if there is a parent file
     */
    fun parentFile(): Boolean {
        return parentFileID != null
    }

    val isValid: Boolean
        /**
         * Validates that required fields are set
         *
         * @return true if all required fields are set
         */
        get() = changelog != null && !changelog!!.trim { it <= ' ' }
            .isEmpty() && releaseType != null && !releaseType!!.trim { it <= ' ' }.isEmpty() &&
                (parentFile() || (gameVersions != null && !gameVersions!!.isEmpty()))

    fun changelog(changelog: String) {
        this.changelog = changelog
        this.changelogType = "text"
    }

    /**
     * Sets HTML format changelog
     *
     * @param htmlChangelog Changelog in HTML format
     */
    fun htmlChangelog(htmlChangelog: String) {
        this.changelog = htmlChangelog
        this.changelogType = "html"
    }

    /**
     * Sets Markdown format changelog
     *
     * @param markdownChangelog Changelog in Markdown format
     */
    fun markdownChangelog(markdownChangelog: String) {
        this.changelog = markdownChangelog
        this.changelogType = "markdown"
    }

    fun gameVersion(version: MinecraftVersion) {
        if (version.canReleaseToCurseForge()) gameVersion(version.id)
    }

    fun gameVersion(gameVersion: Int) {
        if (this.gameVersions == null) this.gameVersions = ArrayList()
        if (!this.gameVersions!!.isEmpty() && this.gameVersions!!.contains(gameVersion)) return
        this.gameVersions!!.add(gameVersion)
    }

    fun alpha() {
        releaseType(ReleaseChannel.Alpha)
    }

    fun beta() {
        releaseType(ReleaseChannel.Beta)
    }

    fun release() {
        releaseType(ReleaseChannel.Release)
    }

    fun releaseType(releaseChannel: ReleaseChannel) {
        this.releaseType = releaseChannel.type
    }

    fun dependency(relation: ProjectRelation) {
        if (this.relations == null) this.relations = Relations()
        if (this.relations!!.projects == null) this.relations!!.projects = ArrayList()
        this.relations!!.addProject(relation)
    }

    fun requiredDependency(slug: String) {
        dependency(required(slug))
    }

    fun requiredDependency(slug: String, projectID: Int) {
        dependency(required(slug, projectID))
    }

    fun optionalDependency(slug: String) {
        dependency(optional(slug))
    }

    fun optionalDependency(slug: String, projectID: Int) {
        dependency(optional(slug, projectID))
    }

    fun embeddedLibrary(slug: String) {
        dependency(embedded(slug))
    }

    fun embeddedLibrary(slug: String, projectID: Int) {
        dependency(embedded(slug, projectID))
    }

    fun incompatible(slug: String) {
        dependency(ProjectRelation.Companion.incompatible(slug))
    }

    fun incompatible(slug: String, projectID: Int) {
        dependency(ProjectRelation.Companion.incompatible(slug, projectID))
    }
}