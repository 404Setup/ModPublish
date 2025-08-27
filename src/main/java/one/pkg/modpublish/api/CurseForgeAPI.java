package one.pkg.modpublish.api;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.internel.PublishResult;
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.network.curseforge.CurseForgeFileData;
import one.pkg.modpublish.data.network.curseforge.CurseForgePublishResult;
import one.pkg.modpublish.data.network.curseforge.ProjectRelation;
import one.pkg.modpublish.settings.properties.Info;
import one.pkg.modpublish.settings.properties.Properties;
import one.pkg.modpublish.util.JsonParser;

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
        String modid = Properties.getPropertiesComponent(project).getValue("modpublish.curseforge.modid");
        Request.Builder requestBuilder = getFormRequest(getRequestBuilder("projects/" + modid + "/upload-file", project));
        RequestBody file = RequestBody.create(data.file(), MediaType.get("application/java-archive"));

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", data.file().getName(), file)
                .addFormDataPart("metadata", createJsonBody(data))
                .build();
        Request request = requestBuilder.post(body).build();

        try (Response resp = client.newCall(request).execute()) {
            Optional<String> status = getStatus(resp);
            if (status.isPresent()) return PublishResult.of(status.get());
            String bs = resp.body().string();
            CurseForgePublishResult result = JsonParser.fromJson(bs, CurseForgePublishResult.class);
            if (result != null && result.isSuccess())
                return new PublishResult("");
            return new PublishResult(bs);
        } catch (IOException e) {
            return PublishResult.of(e.getMessage());
        }
    }

    public String createJsonBody(PublishData data) {
        CurseForgeFileData builder = CurseForgeFileData.create().release()
                .markdownChangelog(data.changelog())
                .displayName(data.versionName());
        for (MinecraftVersion v : data.minecraftVersions()) builder.gameVersion(v);
        if (data.supportedInfo().getClient().isEnabled())
            builder.gameVersion(data.supportedInfo().getClient().getCfid());
        if (data.supportedInfo().getServer().isEnabled())
            builder.gameVersion(data.supportedInfo().getServer().getCfid());
        for (LauncherInfo l : data.loaders()) builder.gameVersion(l.getCfid());
        for (DependencyInfo d : data.dependencies()) {
            ModInfo info = d.getCurseforgeInfo();
            if (info == null || info.modid() == null || info.modid().isBlank() ||
                    info.slug() == null || info.slug().isBlank()) continue;
            ProjectRelation relation = ProjectRelation.create(info.slug(), Integer.parseInt(info.modid()), d.getType());
            builder.dependency(relation);
        }
        return builder.toJson();
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

    @Override
    public Request.Builder getRequestBuilder(String url, Project project) {
        Request.Builder builder = API.super.getRequestBuilder(url, project);
        builder = ab ? builder.header("x-api-key",
                        Properties.getProtectValue(project, "modpublish.curseforge.studioToken").data())
                .url(B_URL + url)
                : builder.header("X-Api-Token",
                        Properties.getProtectValue(project, "modpublish.curseforge.token").data())
                .url(A_URL + url);
        return builder;
    }
}
