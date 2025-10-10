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

object VersionConstraintParser {
    private const val VERSION_PATTERN =
        "\\d+\\.\\d+\\.\\d+(?:-(?:pre|rc)\\d+)?|b\\d+\\.\\d+\\.\\d+|\\d{2}w\\d{2}[a-z](?:_or_[a-z])?|[\\w.-]+"

    private val SIMPLE_VERSION_PATTERN = Regex("^(\\d+(?:\\.\\d+){1,2})$")
    private val EXACT_PATTERN = Regex("^($VERSION_PATTERN)$")
    private val RANGE_PATTERN = Regex("^($VERSION_PATTERN)-($VERSION_PATTERN)$")
    private val EQUAL_PATTERN = Regex("^=($VERSION_PATTERN)$")
    private val TILDE_PATTERN = Regex("^~($VERSION_PATTERN)$")
    private val CARET_PATTERN = Regex("^\\^($VERSION_PATTERN)$")
    private val COMPARISON_PATTERN = Regex("^(>=|<=|>|<)\\s*($VERSION_PATTERN)$")
    private val MAVEN_RANGE_PATTERN = Regex("^[\\[(]([\\w.,-]+)[])]$")
    private val COMPOSITE_PATTERN = Regex("^(.+?)\\s+(.+)$")

    @Throws(IllegalArgumentException::class)
    fun parse(constraintStr: String): VersionConstraint {
        val trimmed = constraintStr.trim()
        require(trimmed.isNotEmpty()) { "Version constraint cannot be empty" }

        // Check for simple version pattern first (e.g., 1.12.2)
        SIMPLE_VERSION_PATTERN.matchEntire(trimmed)?.let {
            return ExactVersionConstraint(it.groupValues[1])
        }

        EQUAL_PATTERN.matchEntire(trimmed)?.let { return ExactVersionConstraint(it.groupValues[1]) }
        RANGE_PATTERN.matchEntire(trimmed)?.let {
            val min = Version(it.groupValues[1])
            val max = Version(it.groupValues[2])
            return RangeConstraint(min, max, includeMin = true, includeMax = true, original = trimmed)
        }
        TILDE_PATTERN.matchEntire(trimmed)?.let { return TildeConstraint(trimmed) }
        CARET_PATTERN.matchEntire(trimmed)?.let { return CaretConstraint(trimmed) }
        COMPARISON_PATTERN.matchEntire(trimmed)?.let {
            return parseComparison(it.groupValues[1], it.groupValues[2], trimmed)
        }
        MAVEN_RANGE_PATTERN.matchEntire(trimmed)?.let {
            return parseMavenRange(trimmed, it.groupValues[1])
        }
        COMPOSITE_PATTERN.matchEntire(trimmed)?.let {
            return try {
                val first = parse(it.groupValues[1])
                val second = parse(it.groupValues[2])
                CompositeConstraint(listOf(first, second), trimmed)
            } catch (_: Exception) {
                throw IllegalArgumentException("Unable to parse version constraint: $constraintStr")
            }
        }

        // Fallback to generic EXACT_PATTERN for other cases
        EXACT_PATTERN.matchEntire(trimmed)?.let { return ExactVersionConstraint(it.groupValues[1]) }

        throw IllegalArgumentException("Unable to parse version constraint: $constraintStr")
    }

    @Throws(IllegalArgumentException::class)
    private fun parseComparison(operator: String, versionStr: String, original: String): VersionConstraint {
        val version = Version(versionStr)
        return when (operator) {
            ">=" -> RangeConstraint(version, null, includeMin = true, includeMax = false, original)
            "<=" -> RangeConstraint(null, version, includeMin = false, includeMax = true, original)
            ">" -> RangeConstraint(version, null, includeMin = false, includeMax = false, original)
            "<" -> RangeConstraint(null, version, includeMin = false, includeMax = false, original)
            else -> throw IllegalArgumentException("Unknown comparison operator: $operator")
        }
    }

    private fun parseMavenRange(original: String, content: String): VersionConstraint {
        val includeMin = original.startsWith("[")
        val includeMax = original.endsWith("]")

        val parts = content.split(",")
        return when (parts.size) {
            1 -> {
                if (original.endsWith(",)")) {
                    val min = Version(parts[0])
                    RangeConstraint(min, null, includeMin, includeMax = false, original)
                } else {
                    ExactVersionConstraint(parts[0])
                }
            }

            2 -> {
                val min = Version(parts[0])
                val max = if (parts[1].isEmpty()) null else Version(parts[1])
                RangeConstraint(min, max, includeMin, includeMax, original)
            }

            else -> {
                val constraints = parts.filter { it.isNotEmpty() }.map { ExactVersionConstraint(it) }
                OrConstraint(constraints, original)
            }
        }
    }
}
