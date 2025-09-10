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

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import okhttp3.*;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.internel.RequestStatus;
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.network.modrinth.ModrinthData;
import one.pkg.modpublish.data.network.modrinth.ProjectRelation;
import one.pkg.modpublish.data.result.PublishResult;
import one.pkg.modpublish.settings.properties.PID;
import one.pkg.modpublish.util.io.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ModrinthAPI implements API {
    private static final String A_URL = "https://api.modrinth.com/v2/";
    private static final String B_URL = "https://staging-api.modrinth.com/v2/";
    private boolean ab = false;

    @Override
    public String getID() {
        return "Modrinth";
    }

    @Override
    public void updateABServer() {
        ab = !ab;
    }

    @Override
    public boolean getABServer() {
        return ab;
    }

    @Override
    @SuppressWarnings("all")
    public PublishResult createVersion(PublishData data, Project project) {
        Request.Builder requestBuilder = getFormRequest(getRequestBuilder("version", project));

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", createJsonBody(data, project));

        for (int i = 0; i < data.files().size(); i++) {
            File f = data.files().get(i);
            String key = i == 0 ? f.getName() + "-primary" : f.getName() + "-" + (i - 1);
            bodyBuilder = bodyBuilder.addFormDataPart(key, f.getName(), RequestBody.create(f, MediaType.get("application/java-archive")));
        }

        Request request = requestBuilder.post(bodyBuilder.build()).build();

        try (Response resp = client.newCall(request).execute()) {
            Optional<String> status = getStatus(resp);
            return status.map(s -> PublishResult.create(this, s)).orElseGet(PublishResult::empty);
        } catch (IOException e) {
            return PublishResult.create(this, e.getMessage());
        }
    }

    @Override
    public String createJsonBody(PublishData data, Project project) {
        var json = ModrinthData.builder().releaseChannel(data.releaseChannel())
                .projectId(ab ? PID.ModrinthTestModID.get(project) : PID.ModrinthModID.get(project))
                .versionBody(data.changelog())
                .status(RequestStatus.Listed)
                .featured(true)
                //.primaryFile(data.file())
                .name(data.versionName())
                .versionNumber(data.versionNumber());
        for (File file : data.files()) json.filePart(file);
        for (LauncherInfo l : data.loaders()) json.loader(l);
        for (MinecraftVersion version : data.minecraftVersions()) json.gameVersion(version);
        for (DependencyInfo d : data.dependencies()) {
            ModInfo info = d.getModrinthModInfo();
            if (info == null || info.modid() == null || info.modid().isBlank() ||
                    info.slug() == null || info.slug().isBlank()) continue;
            json.dependency(
                    ProjectRelation.create(info.modid(), d.getType())
            );
        }
        return json.build().toJson();
    }

    @Override
    public ModInfo getModInfo(String modid, Project project) {
        Request req = getJsonRequest(getRequestBuilder("project/" + modid, project)).get().build();
        try (Response resp = client.newCall(req).execute()) {
            Optional<String> status = getStatus(resp);
            if (status.isPresent()) return ModInfo.of(status.get());
            JsonObject object = JsonParser.getJsonObject(resp.body().byteStream());
            return ModInfo.of(modid, object.get("title").getAsString(), object.get("slug").getAsString());
        } catch (IOException e) {
            return ModInfo.of(e.getMessage());
        }
    }

    public Request.Builder getRequestBuilder(String url, Project project) {
        return getBaseRequestBuilder()
                .header("Authorization",
                        ab ? PID.ModrinthTestToken.getProtect(project).data() :
                                PID.ModrinthToken.getProtect(project).data())
                .url(ab ? B_URL + url : A_URL + url);
    }
}
