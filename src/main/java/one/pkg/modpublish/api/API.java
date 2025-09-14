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
import okhttp3.Response;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.result.PublishResult;
import one.pkg.modpublish.util.resources.Lang;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class API {

    public API() {
    }

    @NotNull
    Request.Builder getBaseRequestBuilder() {
        return new Request.Builder().header("User-Agent", "modpublish/v1 (github.com/404Setup/ModPublish)");
    }

    @NotNull
    public abstract String getID();

    public abstract void updateABServer();

    public abstract boolean getABServer();

    @NotNull
    Request.Builder getJsonRequest(@NotNull Request.Builder builder) {
        return builder.header("Accept", "application/json");
    }

    @NotNull
    Request.Builder getFormRequest(@NotNull Request.Builder builder) {
        return builder.header("Content-Type", "multipart/form-data");
    }

    @NotNull
    Optional<String> getContentType(@NotNull Response response) {
        return Optional.ofNullable(response.header("Content-Type"));
    }

    @NotNull
    abstract String createJsonBody(@NotNull PublishData data, @NotNull Project project);

    @NotNull
    Optional<String> getStatus(@NotNull Response response) {
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

    @NotNull
    public abstract PublishResult createVersion(@NotNull PublishData data, @NotNull Project project);

    @NotNull
    public abstract ModInfo getModInfo(@NotNull String modid, @NotNull Project project);
}
