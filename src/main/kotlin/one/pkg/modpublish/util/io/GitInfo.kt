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
package one.pkg.modpublish.util.io

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepositoryManager

object GitInfo {
    fun Project.getBrach(): String {
        val manager = GitRepositoryManager.getInstance(this)
        for (repository in manager.repositories)
            for (remote in repository.remotes)
                if (remote.name == "origin")
                    return (repository.currentBranchName ?: "")
        return ""
    }
}
