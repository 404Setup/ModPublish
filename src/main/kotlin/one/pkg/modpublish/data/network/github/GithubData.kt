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

package one.pkg.modpublish.data.network.github

import com.google.gson.annotations.SerializedName
import one.pkg.modpublish.data.internal.ReleaseChannel

@Suppress("UNUSED")
data class GithubData(
    @SerializedName("tag_name")
    var tagName: String? = null,

    @SerializedName("target_commitish")
    var targetCommitish: String? = null,

    var name: String? = null,
    var body: String? = null,
    var draft: Boolean = false,
    var prerelease: Boolean = false,

    @SerializedName("generate_release_notes")
    var generateReleaseNotes: Boolean = false,

    @SerializedName("make_latest")
    private var makeLatest: String? = null
) {
    fun makeLatest(): Boolean {
        return "true" == makeLatest
    }

    fun releaseChannel(releaseChannel: ReleaseChannel) {
        this.prerelease = ReleaseChannel.Release != releaseChannel
    }

    fun makeLatest(makeLatest: Boolean) {
        this.makeLatest = if (makeLatest) "true" else "false"
    }
}