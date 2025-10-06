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
package one.pkg.modpublish.util.metadata

import com.intellij.openapi.vfs.VirtualFile

object ModVersion {
    fun VirtualFile.extractVersionNumber(): String {
        val name = nameWithoutExtension
        val extracted = extractVersionFromPattern(name)
        return validateAndNormalizeVersion(extracted)
    }

    private fun extractVersionFromPattern(filename: String): String? {
        listOf('-', '_').forEach { sep ->
            val idx = filename.lastIndexOf(sep)
            if (idx in 1 until filename.length - 1) {
                val candidate = filename.substring(idx + 1)
                if (isValidVersionPattern(candidate)) return candidate
            }
        }

        val pattern = "(\\d+(?:\\.\\d+)*(?:[-.]?(?:alpha|beta|rc|snapshot|dev)\\d*)?)".toRegex(RegexOption.IGNORE_CASE)
        val matches = pattern.findAll(filename).map { it.groupValues[1] }.toList()
        val lastMatch = matches.lastOrNull()
        return if (isValidVersionPattern(lastMatch)) lastMatch else null
    }

    private fun isValidVersionPattern(version: String?): Boolean {
        if (version.isNullOrBlank()) return false
        val v = version.replaceFirst("^[vV]".toRegex(), "")
        val versionPattern = "^\\d+(?:\\.\\d+)*(?:[-.]?(?:alpha|beta|rc|snapshot|dev|final|release)\\d*)?$".toRegex()
        return versionPattern.matches(v)
    }

    private fun validateAndNormalizeVersion(version: String?): String {
        val v = version?.trim()?.replaceFirst("^[vV]".toRegex(), "") ?: return "1.0.0"
        return if (isValidVersionPattern(v)) normalizeVersionFormat(v) else "1.0.0"
    }

    private fun normalizeVersionFormat(version: String): String {
        val parts = version.split("[-.](?=alpha|beta|rc|snapshot|dev|final|release)".toRegex(), 2)
        var mainVersion = parts[0]
        val preRelease = parts.getOrNull(1)

        val versionParts = mainVersion.split(".")
        mainVersion = when (versionParts.size) {
            1 -> "$mainVersion.0.0"
            2 -> "$mainVersion.0"
            else -> mainVersion
        }

        if (!mainVersion.split(".").all { it.toIntOrNull() != null }) return "1.0.0"

        return preRelease?.let { "$mainVersion-$it" } ?: mainVersion
    }
}
