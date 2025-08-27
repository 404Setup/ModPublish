package one.pkg.modpublish.api;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import okhttp3.Request;
import okhttp3.Response;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.internel.PublishResult;
import one.pkg.modpublish.data.internel.RequestStatus;
import one.pkg.modpublish.data.network.modrinth.ModrinthFileData;
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
        return null;
    }

    @Override
    public String createJsonBody(PublishData data, Project project) {
        return ModrinthFileData.create().release()
                .projectId(ab ? PID.ModrinthTestModID.get(project) : PID.ModrinthModID.get(project))
                .changelog(data.changelog())
                .status(RequestStatus.Listed)
                .featured(true)
                .filePart(data.file())
                .primaryFile(data.file())
                .name(data.versionName())
                .versionNumber(data.versionNumber())
                .toJson();
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

    @Override
    public Request.Builder getRequestBuilder(String url, Project project) {
        return API.super.getRequestBuilder(url, project)
                .header("Authorization",
                        ab ? PID.ModrinthTestToken.getProtect(project).data() :
                                PID.ModrinthToken.getProtect(project).data())
                .url(ab ? B_URL : A_URL + url);
    }
}
