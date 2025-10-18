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
class OrConstraint(
    constraints: List<VersionConstraint>,
    override val original: String
) : VersionConstraint {
    private val constraints = constraints.toList()

    override fun satisfies(version: Version): Boolean =
        constraints.any { it.satisfies(version) }

    override val versions: List<String>
        get() = constraints.flatMap { it.versions }

    override val lowVersion: String
        get() = constraints.mapNotNull {
            val v = it.lowVersion.takeIf { s -> s.isNotEmpty() } ?: return@mapNotNull null
            Version(v)
        }.minOrNull()?.toString() ?: ""

    override val maxVersion: String
        get() = constraints.mapNotNull {
            val v = it.maxVersion.takeIf { s -> s.isNotEmpty() } ?: return@mapNotNull null
            Version(v)
        }.maxOrNull()?.toString() ?: ""
}
