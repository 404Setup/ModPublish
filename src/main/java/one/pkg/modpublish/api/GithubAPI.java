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
import one.pkg.modpublish.util.io.JsonParser;

import java.io.IOException;
import java.util.Optional;

public class GithubAPI implements API {
    private static final String RELEASES_URL = "https://api.github.com/repos/{path}/releases";
    private static final String UPLOAD_URL = "https://uploads.github.com/repos/{path}/releases/{id}/assets";
    private static final String REPO_INFO_URL = "https://api.github.com/repos/{path}";
    private static final String BRANCH_COMMIT_URL = "https://api.github.com/repos/{path}/branches/{branch}";

    public Request.Builder getRequestBuilder(String url, Project project) {
        return getBaseRequestBuilder().header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Authorization", "Bearer " + PID.GithubToken.getProtect(project).data())
                .url(url.replace("{path}", PID.GithubRepo.get(project)));
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
        String branch = PID.GithubBranch.get(project);
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
    public PublishResult createVersion(PublishData data, Project project) {
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
                releaseResponse = JsonParser.fromJson(br.result()).getAsJsonObject();
            }

            String uploadUrl = releaseResponse.get("upload_url").getAsString();
            uploadUrl = uploadUrl.split("\\{")[0];

            return uploadAsset(data, project, uploadUrl);

        } catch (Exception e) {
            return PublishResult.create("Failed to create GitHub release: " + e.getMessage());
        }
    }

    private Result createRelease(PublishData data, Project project) {
        try {
            Request request = getJsonRequest(getRequestBuilder(RELEASES_URL, project))
                    .post(RequestBody.create(createJsonBody(data, project), MediaType.get("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                Optional<String> status = getStatus(response);
                if (status.isPresent()) return PublishResult.create("Failed to create release: " + status.get());
                return BackResult.result(response.body().string());
            }
        } catch (IOException e) {
            return PublishResult.create("Network error: " + e.getMessage());
        }
    }

    private PublishResult uploadAsset(PublishData data, Project project, String uploadUrl) {
        try {
            String fileName = data.file().getName();
            String assetUrl = uploadUrl + "?name=" + fileName;

            RequestBody fileBody = RequestBody.create(data.file(), MediaType.get("application/java-archive"));

            Request request = getRequestBuilder(assetUrl, project)
                    .header("Content-Type", "application/java-archive")
                    .post(fileBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                Optional<String> status = getStatus(response);
                /*JsonObject assetResponse = JsonParser.getJsonObject(response.body().byteStream());
                String downloadUrl = assetResponse.get("browser_download_url").getAsString();*/
                return status.map(s -> PublishResult.create("Failed to upload asset: " + s)).orElseGet(PublishResult::empty);


            }
        } catch (IOException e) {
            return PublishResult.create("Failed to upload asset: " + e.getMessage());
        }
    }

    private Optional<JsonObject> checkExistingRelease(String tagName, Project project) {
        try {
            String url = RELEASES_URL + "/tags/" + tagName;
            Request request = getRequestBuilder(url, project)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
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

    private String getTargetCommitish(String branch, Project project) {
        try {
            if (branch == null || branch.trim().isEmpty()) {
                return getDefaultBranch(project);
            } else {
                return getLatestCommitHash(branch, project);
            }
        } catch (Exception e) {
            return branch != null && !branch.trim().isEmpty() ? branch : "main";
        }
    }

    private String getDefaultBranch(Project project) throws IOException {
        Request request = getRequestBuilder(REPO_INFO_URL, project)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonObject repoInfo = JsonParser.fromJson(response.body().string());
                return repoInfo.get("default_branch").getAsString();
            }
        }
        return "main";
    }

    private String getLatestCommitHash(String branch, Project project) throws IOException {
        String url = BRANCH_COMMIT_URL.replace("{branch}", branch);
        Request request = getRequestBuilder(url, project)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonObject branchInfo = JsonParser.fromJson(response.body().string());
                JsonObject commit = branchInfo.getAsJsonObject("commit");
                return commit.get("sha").getAsString();
            }
        }
        return branch;
    }


    @Override
    public ModInfo getModInfo(String modid, Project project) {
        return null;
    }
}