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

package one.pkg.modpublish.data.network.gitlab

import com.google.gson.annotations.SerializedName
import one.pkg.modpublish.data.internal.ReleaseChannel

@Suppress("UNUSED")
data class GitlabData(
    @SerializedName("tag_name")
    var tagName: String? = null,

    @SerializedName("ref")
    var ref: String? = null,

    var name: String? = null,
    var description: String? = null,

    @SerializedName("released_at")
    var releasedAt: String? = null,

    var milestones: List<String>? = null
) {
    fun setReleaseChannel(releaseChannel: ReleaseChannel) {
        // GitLab doesn't have a prerelease flag like GitHub
        // The release channel can be indicated in the tag name or description
        // For now, we'll just store it for potential use in milestones or description
    }
}
