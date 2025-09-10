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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.result.PublishResult;
import one.pkg.modpublish.util.proxy.MProxy;
import one.pkg.modpublish.util.resources.Lang;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface API {
    OkHttpClient client = new OkHttpClient.Builder().proxy(MProxy.getProxy())
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            //.hostnameVerifier(SSLSocketClient.getHostnameVerifier())
            //.sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
            .build();

    default Request.Builder getBaseRequestBuilder() {
        return new Request.Builder().header("User-Agent", "modpublish/v1 (github.com/404Setup/ModPublish)");
    }

    String getID();

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
            return Optional.of(Lang.get("api.common.err.403"));
        if (response.code() == 404)
            return Optional.of(Lang.get("api.common.err.404"));
        if (response.code() == 500)
            return Optional.of(Lang.get("api.common.err.500"));
        if (response.code() == 302)
            return Optional.of("Duplicate resource");
        try {
            if (response.code() == 400 || response.code() == 401 || response.code() == 422)
                return Optional.of(response.body().string());
        } catch (Exception ignored) {
            return Optional.of("HTTP " + response.code());
        }
        Optional<String> ct = getContentType(response);
        if (ct.isEmpty() || !ct.get().contains("application/json"))
            return Optional.of(Lang.get("api.common.err.format", ct.orElse("Unknown")));
        return Optional.empty();
    }

    PublishResult createVersion(PublishData data, Project project);

    ModInfo getModInfo(String modid, Project project);
}
