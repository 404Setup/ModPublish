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

@Suppress("UNUSED")
class CompositeConstraint(
    constraints: List<VersionConstraint>,
    override val original: String
) : VersionConstraint {
    private val constraints: List<VersionConstraint> = constraints.toList()

    override fun satisfies(version: Version): Boolean =
        constraints.all { it.satisfies(version) }

    override val versions: List<String>
        get() = constraints.flatMap { it.versions }

    override val lowVersion: String
        get() {
            val lows = constraints.mapNotNull { it.lowVersion.takeIf { s -> s.isNotEmpty() } }
                .map { Version(it) }
            return lows.maxOrNull()?.toString() ?: ""
        }

    override val maxVersion: String
        get() {
            val highs = constraints.mapNotNull { it.maxVersion.takeIf { s -> s.isNotEmpty() } }
                .map { Version(it) }
            return highs.minOrNull()?.toString() ?: ""
        }
}
