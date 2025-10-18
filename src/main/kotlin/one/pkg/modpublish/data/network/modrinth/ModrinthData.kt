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
import one.pkg.modpublish.data.internal.PublishType
import one.pkg.modpublish.data.internal.ReleaseChannel
import one.pkg.modpublish.data.internal.RequestStatus
import one.pkg.modpublish.data.local.MinecraftVersion
import one.pkg.modpublish.data.network.modrinth.ProjectRelation.Companion.createEmbedded
import one.pkg.modpublish.data.network.modrinth.ProjectRelation.Companion.createIncompatible
import one.pkg.modpublish.data.network.modrinth.ProjectRelation.Companion.createOptional
import one.pkg.modpublish.data.network.modrinth.ProjectRelation.Companion.createRequired
import one.pkg.modpublish.util.io.JsonParser.fromJson
import java.io.File

/**
 * Comment: Why are many parameters here inconsistent with the API docs?
 */
@Suppress("UNUSED")
data class ModrinthData(
    @SerializedName("version_title")
    var name: String? = null,

    @SerializedName("version_number")
    var versionNumber: String? = null,

    @SerializedName("version_body")
    var versionBody: String? = null,
    var dependencies: MutableList<ProjectRelation> = arrayListOf(),

    @SerializedName("game_versions")
    var gameVersions: MutableList<String?>? = null,

    /**
     * The release channel for this version
     *
     * Allowed values: release, beta, alpha
     */
    @SerializedName("release_channel")
    var releaseChannel: String? = null,
    var loaders: MutableList<String?>? = null,

    var featured: Boolean = true,

    /**
     * Allowed values: listed, archived, draft, unlisted, scheduled
     */
    var status: String? = null,

    /**
     * Allowed values: listed, archived, draft, unlisted
     */
    @SerializedName("requested_status")
    var requestedStatus: String? = null,

    @SerializedName("project_id")
    var projectId: String? = null,

    /**
     * An array of the multipart field names of each file that goes with this version
     */
    @SerializedName("file_parts")
    var fileParts: MutableList<String>? = null,

    /**
     * The multipart field name of the primary file
     */
    @SerializedName("primary_file")
    var primaryFile: String? = null,

    @SerializedName("file_types")
    var fileTypes: MutableList<String>? = null
) {

    constructor() : this(null)

    val isValid: Boolean
        get() = !projectId!!.trim { it <= ' ' }.isEmpty() || !versionNumber!!.trim { it <= ' ' }
            .isEmpty() || gameVersions != null
                && !gameVersions!!.isEmpty() || !name!!.trim { it <= ' ' }.isEmpty() || fileParts != null &&
                !fileParts!!.isEmpty() || primaryFile != null && !primaryFile!!.trim { it <= ' ' }.isEmpty()

    /*public String primaryFile() {
        return primaryFile;
    }

    public ModrinthFileData primaryFile(String primaryFile) {
        this.primaryFile = primaryFile + "-primary";
        return this;
    }

    public ModrinthFileData primaryFile(File primaryFile) {
        return primaryFile(primaryFile.getName());
    }*/

    fun dependency(dependency: ProjectRelation) {
        for (rel in this.dependencies) if (rel.projectID == dependency.projectID) return
        this.dependencies.add(dependency)
    }

    fun requiredDependency(slug: String) {
        return dependency(createRequired(slug))
    }

    fun optionalDependency(slug: String) {
        return dependency(createOptional(slug))
    }

    fun embeddedLibrary(slug: String) {
        return dependency(createEmbedded(slug))
    }

    fun incompatible(slug: String) {
        return dependency(createIncompatible(slug))
    }

    fun gameVersion(gameVersion: String) {
        if (this.gameVersions == null) this.gameVersions = ArrayList()
        if (!this.gameVersions!!.isEmpty() && this.gameVersions!!.contains(gameVersion)) return
        this.gameVersions!!.add(gameVersion)
    }

    fun gameVersion(gameVersion: MinecraftVersion) {
        if (this.gameVersions == null) this.gameVersions = ArrayList()
        if (!this.gameVersions!!.isEmpty() && this.gameVersions!!.contains(gameVersion.version)) return
        this.gameVersions!!.add(gameVersion.version)
    }

    fun releaseChannel(type: ReleaseChannel) {
        this.releaseChannel = type.type
    }

    fun release() {
        return releaseChannel(ReleaseChannel.Release)
    }

    fun beta() {
        return releaseChannel(ReleaseChannel.Beta)
    }

    fun alpha() {
        return releaseChannel(ReleaseChannel.Alpha)
    }

    fun loader(loader: String) {
        if (this.loaders == null) this.loaders = ArrayList()
        if (!this.loaders!!.isEmpty() && this.loaders!!.contains(loader)) return
        this.loaders!!.add(loader)
    }

    fun loader(info: PublishType) {
        return loader(info.getID())
    }

    fun status(type: RequestStatus) {
        this.status = type.status
    }

    fun requestedStatus(type: RequestStatus) {
        if (type != RequestStatus.Scheduled)
            this.requestedStatus = type.status
    }

    fun filePart(filePart: String) {
        if (this.fileParts == null) this.fileParts = ArrayList()
        if (!fileParts!!.isEmpty() && this.fileParts!!.contains(filePart)) return
        if (this.fileParts!!.isEmpty()) this.fileParts!!.add("$filePart-primary")
        else {
            val i = this.fileParts!!.size - 1
            this.fileParts!!.add("$filePart-$i")
        }
    }

    fun filePart(file: File) {
        filePart(file.getName())
    }

    companion object {
        fun fromJson(json: String): ModrinthData {
            return json.fromJson(ModrinthData::class.java)
        }
    }
}
