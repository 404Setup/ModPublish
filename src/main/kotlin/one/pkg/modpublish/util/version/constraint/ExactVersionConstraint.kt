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
package one.pkg.modpublish.util.version.constraint

import one.pkg.modpublish.util.version.Version

@Suppress("unused")
class ExactVersionConstraint(versionStr: String) : VersionConstraint {
    private val targetVersion = Version(versionStr)
    override val original: String = versionStr

    override fun satisfies(version: Version): Boolean = targetVersion == version

    override val versions: List<String>
        get() = listOf(targetVersion.toString())

    override val lowVersion: String
        get() = targetVersion.toString()

    override val maxVersion: String
        get() = targetVersion.toString()
}
