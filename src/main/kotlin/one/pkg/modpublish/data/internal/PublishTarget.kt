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
package one.pkg.modpublish.data.internal

import one.pkg.modpublish.api.API
import one.pkg.modpublish.api.CurseForgeAPI
import one.pkg.modpublish.api.EmptyAPI
import one.pkg.modpublish.api.GithubAPI
import one.pkg.modpublish.api.GitlabAPI
import one.pkg.modpublish.api.ModrinthAPI

enum class PublishTarget(val api: API) {
    Modrinth(ModrinthAPI()), CurseForge(CurseForgeAPI()),
    Github(GithubAPI()), Gitlab(GitlabAPI()),
    Empty(EmptyAPI());
}
