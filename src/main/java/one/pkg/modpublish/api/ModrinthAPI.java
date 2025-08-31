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
import one.pkg.modpublish.data.result.PublishResult;
import one.pkg.modpublish.data.internel.RequestStatus;
import one.pkg.modpublish.data.local.DependencyInfo;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.data.network.modrinth.ModrinthFileData;
import one.pkg.modpublish.data.network.modrinth.ProjectRelation;
import one.pkg.modpublish.settings.properties.PID;
import one.pkg.modpublish.util.JsonParser;

import java.io.IOException;
import java.util.Optional;

public class ModrinthAPI implements API {
    private static final String A_URL = "https://api.modrinth.com/v2/";
    private static final String B_URL = "https://staging-api.modrinth.com/v2/";
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
        Request.Builder requestBuilder = getFormRequest(getRequestBuilder("version", project));
        RequestBody file = RequestBody.create(data.file(), MediaType.get("application/java-archive"));

        String primaryFileKey = data.file().getName()+"-primary";
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", createJsonBody(data, project))
                .addFormDataPart(primaryFileKey, data.file().getName(), file)
                .build();
        Request request = requestBuilder.post(body).build();

        try (Response resp = client.newCall(request).execute()) {
            Optional<String> status = getStatus(resp);
            return status.map(PublishResult::create).orElseGet(PublishResult::empty);
        } catch (IOException e) {
            return PublishResult.of(e.getMessage());
        }
    }

    @Override
    public String createJsonBody(PublishData data, Project project) {
        var json = ModrinthFileData.create().releaseChannel(data.releaseChannel())
                .projectId(ab ? PID.ModrinthTestModID.get(project) : PID.ModrinthModID.get(project))
                .versionBody(data.changelog())
                .status(RequestStatus.Listed)
                .featured(true)
                .filePart(data.file())
                //.primaryFile(data.file())
                .name(data.versionName())
                .versionNumber(data.versionNumber());
        for (LauncherInfo l : data.loaders()) json.loader(l);
        for (MinecraftVersion version : data.minecraftVersions()) json.gameVersion(version);
        for (DependencyInfo d : data.dependencies()) {
            ModInfo info = d.getModrinthInfo();
            if (info == null || info.modid() == null || info.modid().isBlank() ||
                    info.slug() == null || info.slug().isBlank()) continue;
            json.dependency(
                    ProjectRelation.create(info.modid(), d.getType())
            );
        }
        return json.toJson();
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
