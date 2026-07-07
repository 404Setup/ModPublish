/*
 * Copyright (C) 2025 - 2026 404Setup (https://github.com/404Setup)
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
package one.pkg.modpublish.api

import com.intellij.openapi.project.Project

abstract class GitPlatformAPI : API() {
    protected abstract suspend fun getDefaultBranch(project: Project): String
    protected abstract suspend fun getLatestCommitHash(branch: String, project: Project): String

    protected suspend fun getTargetCommitish(branch: String, project: Project): String = try {
        branch.takeIf { it.isNotBlank() }?.let { getLatestCommitHash(it, project) } ?: getDefaultBranch(project)
    } catch (_: Exception) {
        branch.takeIf { it.isNotBlank() } ?: "main"
    }
}
