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

class CaretConstraint(versionStr: String) : VersionConstraint {
    private val baseVersion = Version(versionStr.substring(1))
    override val original: String = versionStr

    override fun satisfies(version: Version): Boolean =
        version >= baseVersion && version < Version(baseVersion.major + 1, 0, 0)

    override val versions: List<String>
        get() = listOf(">=$baseVersion <${baseVersion.major + 1}.0.0")

    override val lowVersion: String
        get() = baseVersion.toString()

    override val maxVersion: String
        get() = "${baseVersion.major + 1}.0.0"
}
