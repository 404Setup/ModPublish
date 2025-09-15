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
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import one.pkg.modpublish.data.internel.ModInfo;
import one.pkg.modpublish.data.internel.PublishData;
import one.pkg.modpublish.data.network.github.GithubData;
import one.pkg.modpublish.data.result.BackResult;
import one.pkg.modpublish.data.result.PublishResult;
import one.pkg.modpublish.data.result.Result;
import one.pkg.modpublish.settings.properties.PID;
import one.pkg.modpublish.util.io.GitInfo;
import one.pkg.modpublish.util.io.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GithubAPI extends API {
    private static final String RELEASES_URL = "https://api.github.com/repos/{path}/releases";
    private static final String UPLOAD_URL = "https://uploads.github.com/repos/{path}/releases/{id}/assets";
    private static final String REPO_INFO_URL = "https://api.github.com/repos/{path}";
    private static final String BRANCH_COMMIT_URL = "https://api.github.com/repos/{path}/branches/{branch}";

    @NotNull
    public Request.Builder getRequestBuilder(@NotNull String url, @NotNull Project project) {
        return getBaseRequestBuilder().header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Authorization", "Bearer " + PID.GithubToken.getProtect(project).data())
                .url(url.replace("{path}", PID.GithubRepo.get(project)));
    }

    @Override
    public @NotNull String getID() {
        return "Github";
    }

    @Override
    public void updateABServer() {
    }

    @Override
    public boolean getABServer() {
        return true;
    }

    @Override
    @NotNull
    String createJsonBody(@NotNull PublishData data, @NotNull Project project) {
        String branch = PID.GithubBranch.get(project);
        if (branch.isEmpty()) branch = GitInfo.getBrach(project);
        String targetCommitish = getTargetCommitish(branch, project);

        GithubData.GithubDataBuilder githubData = GithubData.builder()
                .tagName(data.versionNumber().startsWith("v") ?
                        data.versionNumber() :
                        "v" + data.versionNumber())
                .targetCommitish(targetCommitish)
                .name(data.versionName())
                .body(data.changelog())
                .releaseChannel(data.releaseChannel())
                .draft(false)
                .generateReleaseNotes(true)
                .makeLatest(true);

        return githubData.build().toJson();
    }

    @Override
    public @NotNull PublishResult createVersion(@NotNull PublishData data, @NotNull Project project) {
        try {
            String tagName = data.versionNumber().startsWith("v") ?
                    data.versionNumber() :
                    "v" + data.versionNumber();

            Optional<JsonObject> existingRelease = checkExistingRelease(tagName, project);

            JsonObject releaseResponse;
            if (existingRelease.isPresent()) {
                releaseResponse = existingRelease.get();
            } else {
                Result releaseResult = createRelease(data, project);
                if (releaseResult instanceof PublishResult pr) {
                    return pr;
                }
                BackResult br = (BackResult) releaseResult;
                releaseResponse = JsonParser.fromJson(br.asString()).getAsJsonObject();
            }

            String uploadUrl = releaseResponse.get("upload_url").getAsString();
            uploadUrl = uploadUrl.split("\\{")[0];

            PublishResult result = PublishResult.empty();
            for (File file : data.files()) {
                result = uploadAsset(file, project, uploadUrl);
                if (!result.isSuccess()) break;
            }

            return result;

        } catch (Exception e) {
            return PublishResult.create(this, "Failed to create GitHub release: " + e.getMessage());
        }
    }

    @NotNull
    private Result createRelease(@NotNull PublishData data, @NotNull Project project) {
        try {
            Request request = getJsonRequest(getRequestBuilder(RELEASES_URL, project))
                    .post(RequestBody.create(createJsonBody(data, project), MediaType.get("application/json")))
                    .build();

            try (Response response = NetworkUtil.client.newCall(request).execute()) {
                Optional<String> status = getStatus(response);
                if (status.isPresent()) return PublishResult.create(this, "Failed to create release: " + status.get());
                return BackResult.result(response.body().string());
            }
        } catch (IOException e) {
            return PublishResult.create(this, "Network error: " + e.getMessage());
        }
    }

    @NotNull
    private PublishResult uploadAsset(@NotNull File file, @NotNull Project project, @NotNull String uploadUrl) {
        try {
            String fileName = file.getName();
            String assetUrl = uploadUrl + "?name=" + fileName;

            RequestBody fileBody = RequestBody.create(file, MediaType.get("application/java-archive"));

            Request request = getRequestBuilder(assetUrl, project)
                    .header("Content-Type", "application/java-archive")
                    .post(fileBody)
                    .build();

            try (Response response = NetworkUtil.client.newCall(request).execute()) {
                Optional<String> status = getStatus(response);
                /*JsonObject assetResponse = JsonParser.getJsonObject(response.body().byteStream());
                String downloadUrl = assetResponse.get("browser_download_url").getAsString();*/
                return status.map(s -> PublishResult.create(this, "Failed to upload asset: " + s)).orElseGet(PublishResult::empty);
            }
        } catch (IOException e) {
            return PublishResult.create(this, "Failed to upload asset: " + e.getMessage());
        }
    }

    @NotNull
    private Optional<JsonObject> checkExistingRelease(@NotNull String tagName, @NotNull Project project) {
        try {
            String url = RELEASES_URL + "/tags/" + tagName;
            Request request = getRequestBuilder(url, project)
                    .get()
                    .build();

            try (Response response = NetworkUtil.client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject releaseInfo = JsonParser.fromJson(responseBody);
                    return Optional.of(releaseInfo);
                } else if (response.code() == 404) {
                    return Optional.empty();
                } else {
                    return Optional.empty();
                }
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @NotNull
    private String getTargetCommitish(@NotNull String branch, @NotNull Project project) {
        try {
            if (branch.trim().isEmpty()) {
                return getDefaultBranch(project);
            } else {
                return getLatestCommitHash(branch, project);
            }
        } catch (Exception e) {
            return !branch.trim().isEmpty() ? branch : "main";
        }
    }

    @NotNull
    private String getDefaultBranch(@NotNull Project project) throws IOException {
        Request request = getRequestBuilder(REPO_INFO_URL, project)
                .get()
                .build();

        try (Response response = NetworkUtil.client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonObject repoInfo = JsonParser.fromJson(response.body().string());
                return repoInfo.get("default_branch").getAsString();
            }
        }
        return "main";
    }

    @NotNull
    private String getLatestCommitHash(@NotNull String branch, @NotNull Project project) throws IOException {
        String url = BRANCH_COMMIT_URL.replace("{branch}", branch);
        Request request = getRequestBuilder(url, project)
                .get()
                .build();

        try (Response response = NetworkUtil.client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonObject branchInfo = JsonParser.fromJson(response.body().string());
                JsonObject commit = branchInfo.getAsJsonObject("commit");
                return commit.get("sha").getAsString();
            }
        }
        return branch;
    }


    @Override
    public @NotNull ModInfo getModInfo(@NotNull String modid, @NotNull Project project) {
        return ModInfo.empty();
    }
}