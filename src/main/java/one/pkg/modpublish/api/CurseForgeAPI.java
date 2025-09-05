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
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.network.curseforge.CurseForgeData;
import one.pkg.modpublish.data.network.curseforge.CurseForgePublishResult;
import one.pkg.modpublish.data.network.curseforge.ProjectRelation;
import one.pkg.modpublish.data.result.PublishResult;
import one.pkg.modpublish.settings.properties.PID;
import one.pkg.modpublish.util.io.JsonParser;

import java.io.IOException;
import java.util.Optional;

public class CurseForgeAPI implements API {
    private static final String A_URL = "https://minecraft.curseforge.com/api/";
    private static final String B_URL = "https://api.curseforge.com/v1/";
    private boolean ab = false;

    @Override
    public void updateABServer() {
        ab = !ab;
    }

    @Override
    public boolean getABServer() {
        return ab;
    }

    @Override
    public PublishResult createVersion(PublishData data, Project project) {
        if (ab) ab = false;
        String modid = PID.CurseForgeModID.get(project);
        Request.Builder requestBuilder = getFormRequest(getRequestBuilder("projects/" + modid + "/upload-file", project));
        RequestBody file = RequestBody.create(data.files().get(0), MediaType.get("application/java-archive"));

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", data.files().get(0).getName(), file)
                .addFormDataPart("metadata", createJsonBody(data, project))
                .build();
        Request request = requestBuilder.post(body).build();

        try (Response resp = client.newCall(request).execute()) {
            Optional<String> status = getStatus(resp);
            if (status.isPresent()) return PublishResult.of(status.get());
            String bs = resp.body().string();
            CurseForgePublishResult result = JsonParser.fromJson(bs, CurseForgePublishResult.class);
            if (result != null && result.isSuccess())
                return PublishResult.empty();
            return new PublishResult(bs);
        } catch (IOException e) {
            return PublishResult.of(e.getMessage());
        }
    }

    @Override
    public String createJsonBody(PublishData data, Project project) {
        CurseForgeData.CurseForgeDataBuilder builder = CurseForgeData.builder().releaseType(data.releaseChannel())
                .markdownChangelog(data.changelog())
                .displayName(data.versionName());
        for (MinecraftVersion v : data.minecraftVersions()) builder.gameVersion(v);
        if (data.supportedInfo().getClient().isEnabled())
            builder.gameVersion(data.supportedInfo().getClient().getCfid());
        if (data.supportedInfo().getServer().isEnabled())
            builder.gameVersion(data.supportedInfo().getServer().getCfid());
        for (LauncherInfo l : data.loaders()) if (l.getCfid() > 0) builder.gameVersion(l.getCfid());
        for (DependencyInfo d : data.dependencies()) {
            ModInfo info = d.getCurseforgeModInfo();
            if (info == null || info.modid() == null || info.modid().isBlank() ||
                    info.slug() == null || info.slug().isBlank()) continue;
            ProjectRelation relation = ProjectRelation.create(info.slug(), Integer.parseInt(info.modid()), d.getType());
            builder.dependency(relation);
        }
        return builder.build().toJson();
    }

    @Override
    public ModInfo getModInfo(String modid, Project project) {
        if (!ab) ab = true;
        Request req = getJsonRequest(getRequestBuilder("mods/" + modid, project)).get().build();
        try (Response resp = client.newCall(req).execute()) {
            Optional<String> status = getStatus(resp);
            if (status.isPresent()) return ModInfo.of(status.get());
            JsonObject object = JsonParser.getJsonObject(resp.body().byteStream());
            JsonObject data = object.getAsJsonObject("data");
            return ModInfo.of(modid, data.get("name").getAsString(), data.get("slug").getAsString());
        } catch (IOException e) {
            return ModInfo.of(e.getMessage());
        }
    }

    public Request.Builder getRequestBuilder(String url, Project project) {
        Request.Builder builder = getBaseRequestBuilder();
        return ab ? builder.header("x-api-key",
                        PID.CurseForgeStudioToken.getProtect(project).data())
                .url(B_URL + url)
                : builder.header("X-Api-Token",
                        PID.CurseForgeToken.getProtect(project).data())
                .url(A_URL + url);
    }
}
