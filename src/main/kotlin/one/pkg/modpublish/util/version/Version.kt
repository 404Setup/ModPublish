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
package one.pkg.modpublish.util.version

data class Version(
    val original: String
) : Comparable<Version> {
    val type: VersionType
    val major: Int
    val minor: Int
    val patch: Int
    val preRelease: String?

    companion object {
        private val RELEASE_PATTERN = Regex("^(\\d+)\\.(\\d+)\\.(\\d+)$")
        private val PRE_RELEASE_PATTERN = Regex("^(\\d+)\\.(\\d+)\\.(\\d+)-(pre|rc)(\\d+)$")
        private val BETA_PATTERN = Regex("^b(\\d+)\\.(\\d+)\\.(\\d+)$")
        private val SNAPSHOT_PATTERN = Regex("^(\\d{2})w(\\d{2})([a-z])(?:_or_([a-z]))?$")
    }

    init {
        val trimmed = original.trim()
        var maj = 0
        var min = 0
        var pat = 0
        var pre: String? = null
        var verType = VersionType.UNKNOWN

        when {
            RELEASE_PATTERN.matches(trimmed) -> {
                val (a, b, c) = RELEASE_PATTERN.find(trimmed)!!.destructured
                maj = a.toInt(); min = b.toInt(); pat = c.toInt()
                verType = VersionType.RELEASE
            }

            PRE_RELEASE_PATTERN.matches(trimmed) -> {
                val (a, b, c, t, n) = PRE_RELEASE_PATTERN.find(trimmed)!!.destructured
                maj = a.toInt(); min = b.toInt(); pat = c.toInt()
                pre = t + n
                verType = if (t == "rc") VersionType.RELEASE_CANDIDATE else VersionType.PRE_RELEASE
            }

            BETA_PATTERN.matches(trimmed) -> {
                val (a, b, c) = BETA_PATTERN.find(trimmed)!!.destructured
                maj = a.toInt(); min = b.toInt(); pat = c.toInt()
                verType = VersionType.BETA
            }

            SNAPSHOT_PATTERN.matches(trimmed) -> {
                val (y, w, l, _) = SNAPSHOT_PATTERN.find(trimmed)!!.destructured
                maj = 2000 + y.toInt()
                min = w.toInt()
                pat = l[0] - 'a'
                verType = VersionType.SNAPSHOT
            }

            else -> { // fallback
                val parts = trimmed.split(".")
                maj = parts.getOrNull(0)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
                min = parts.getOrNull(1)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
                pat = parts.getOrNull(2)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            }
        }

        major = maj
        minor = min
        patch = pat
        preRelease = pre
        type = verType
    }

    constructor(major: Int, minor: Int, patch: Int) : this("$major.$minor.$patch")

    override fun compareTo(other: Version): Int {
        compareValuesBy(this, other, Version::major, Version::minor, Version::patch).let {
            if (it != 0) return it
        }
        compareValuesBy(this, other) { it.typePriority() }.let {
            if (it != 0) return it
        }
        return compareValues(preRelease ?: "", other.preRelease ?: "")
    }

    private fun typePriority(): Int = when (type) {
        VersionType.BETA -> 1
        VersionType.SNAPSHOT -> 2
        VersionType.PRE_RELEASE -> 3
        VersionType.RELEASE_CANDIDATE -> 4
        VersionType.RELEASE -> 5
        VersionType.UNKNOWN -> 0
    }

    override fun toString(): String = original

    enum class VersionType {
        RELEASE,
        PRE_RELEASE,
        RELEASE_CANDIDATE,
        BETA,
        SNAPSHOT,
        UNKNOWN
    }
}
