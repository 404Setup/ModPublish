package one.pkg.modpublish.api;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import okhttp3.Request;
import okhttp3.Response;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.internel.PublishResult;
import one.pkg.modpublish.settings.properties.Properties;
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
        return new Request.Builder()
                .header("Authorization",
                        ab ? Properties.getProtectValue(project,"modpublish.modrinth.testToken").data() :
                                Properties.getProtectValue(project,"modpublish.modrinth.token").data())
                .header("User-Agent", "modpublish/v1 (github.com/404Setup/ModPublish)")
                .url(ab ? B_URL : A_URL + url);
    }
}
