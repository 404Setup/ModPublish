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

package one.pkg.modpublish.util.io;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

public class GitInfo {
    public static @NotNull String getBrach(Project project) {
        @NotNull GitRepositoryManager manager = GitRepositoryManager.getInstance(project);
        for (GitRepository repository : manager.getRepositories()) {
            for (GitRemote remote : repository.getRemotes()) {
                if (remote.getName().equals("origin")) {
                    return repository.getCurrentBranchName() == null ? "" : repository.getCurrentBranchName();
                }
            }
        }
        return "";
    }
}
