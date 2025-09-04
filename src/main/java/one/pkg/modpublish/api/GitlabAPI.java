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

package one.pkg.modpublish.api;

import com.intellij.openapi.project.Project;
import okhttp3.Request;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.result.PublishResult;

@SuppressWarnings("unused")
public class GitlabAPI implements API {
    public Request.Builder getRequestBuilder(String url, Project project) {
        return null;
    }

    @Override
    public void updateABServer() {

    }

    @Override
    public boolean getABServer() {
        return true;
    }

    @Override
    public String createJsonBody(PublishData data, Project project) {
        return "";
    }

    @Override
    public PublishResult createVersion(PublishData data, Project project) {
        return null;
    }

    @Override
    public ModInfo getModInfo(String modid, Project project) {
        return null;
    }
}
