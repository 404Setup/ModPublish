package one.pkg.modpublish.api;

import com.intellij.openapi.project.Project;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.internel.PublishResult;
import one.pkg.modpublish.resources.Lang;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface API {
    OkHttpClient client = new OkHttpClient.Builder()/*.proxy(ProxyConfigReader.getProxy(Proxy.NO_PROXY))*/
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            //.hostnameVerifier(SSLSocketClient.getHostnameVerifier())
            //.sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
            .build();

    default Request.Builder getRequestBuilder(String url, Project project) {
        return new Request.Builder().header("User-Agent", "modpublish/v1 (github.com/404Setup/ModPublish)");
    }

    void updateABServer();

    boolean getABServer();

    default Request.Builder getJsonRequest(Request.Builder builder) {
        return builder.header("Accept", "application/json");
    }

    default Request.Builder getFormRequest(Request.Builder builder) {
        return builder.header("Content-Type", "multipart/form-data");
    }

    default Optional<String> getContentType(Response response) {
        return Optional.ofNullable(response.header("Content-Type"));
    }

    String createJsonBody(PublishData data, Project project);

    default Optional<String> getStatus(Response response) {
        if (response.code() == 403)
            return Optional.of(Lang.get("api.curseforge.err.403"));
        if (response.code() == 404)
            return Optional.of(Lang.get("api.common.err.404"));
        if (response.code() == 500)
            return Optional.of(Lang.get("api.common.err.500"));
        Optional<String> ct = getContentType(response);
        if (ct.isEmpty() || !ct.get().contains("application/json"))
            return Optional.of(Lang.get("api.common.err.format", ct.orElse("Unknown")));
        return Optional.empty();
    }

    PublishResult createVersion(PublishData data, Project project);

    ModInfo getModInfo(String modid, Project project);
}
