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
package one.pkg.modpublish.data.internal

import one.pkg.modpublish.data.local.DependencyInfo
import one.pkg.modpublish.data.local.LauncherInfo
import one.pkg.modpublish.data.local.MinecraftVersion
import one.pkg.modpublish.data.local.SupportedInfo
import java.io.File

@JvmRecord
data class PublishData(
    val versionName: String, val versionNumber: String, val enabled: Selector,
    val releaseChannel: ReleaseChannel, val loaders: List<LauncherInfo>,
    val supportedInfo: SupportedInfo, val minecraftVersions: List<MinecraftVersion>,
    val changelog: String, val dependencies: List<DependencyInfo>,
    val files: Array<File>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublishData

        if (versionName != other.versionName) return false
        if (versionNumber != other.versionNumber) return false
        if (enabled != other.enabled) return false
        if (releaseChannel != other.releaseChannel) return false
        if (loaders != other.loaders) return false
        if (supportedInfo != other.supportedInfo) return false
        if (minecraftVersions != other.minecraftVersions) return false
        if (changelog != other.changelog) return false
        if (dependencies != other.dependencies) return false
        if (!files.contentEquals(other.files)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = versionName.hashCode()
        result = 31 * result + versionNumber.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + releaseChannel.hashCode()
        result = 31 * result + loaders.hashCode()
        result = 31 * result + supportedInfo.hashCode()
        result = 31 * result + minecraftVersions.hashCode()
        result = 31 * result + changelog.hashCode()
        result = 31 * result + dependencies.hashCode()
        result = 31 * result + files.contentHashCode()
        return result
    }
}