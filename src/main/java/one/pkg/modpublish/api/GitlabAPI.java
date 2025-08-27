package one.pkg.modpublish.api;

import com.intellij.openapi.project.Project;
import okhttp3.Request;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.internel.PublishResult;

public class GitlabAPI implements API {
    @Override
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
