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
class RangeConstraint(
    private val minVer: Version?,
    private val maxVer: Version?,
    private val includeMin: Boolean,
    private val includeMax: Boolean,
    override val original: String
) : VersionConstraint {
    override fun satisfies(version: Version): Boolean {
        minVer?.let {
            val cmp = version.compareTo(it)
            if ((includeMin && cmp < 0) || (!includeMin && cmp <= 0)) return false
        }

        maxVer?.let {
            val cmp = version.compareTo(it)
            if ((includeMax && cmp > 0) || (!includeMax && cmp >= 0)) return false
        }

        return true
    }

    override val versions: List<String>
        get() = listOf(buildString {
            minVer?.let { append(if (includeMin) ">=" else ">").append(it) }
            maxVer?.let {
                if (isNotEmpty()) append(" ")
                append(if (includeMax) "<=" else "<").append(it)
            }
        })

    override val lowVersion: String
        get() = minVer?.toString() ?: ""

    override val maxVersion: String
        get() = maxVer?.toString() ?: ""
}
