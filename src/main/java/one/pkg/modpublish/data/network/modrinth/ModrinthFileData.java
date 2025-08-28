package one.pkg.modpublish.data.network.modrinth;

import com.google.gson.annotations.SerializedName;
import one.pkg.modpublish.data.internel.ReleaseType;
import one.pkg.modpublish.data.internel.RequestStatus;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.util.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment: Why are many parameters here inconsistent with the API docs?
 * */
@SuppressWarnings("unused")
public class ModrinthFileData {
    @SerializedName("version_title")
    @NotNull
    private String name;
    @SerializedName("version_number")
    @NotNull
    private String versionNumber;
    @SerializedName("version_body")
    private String versionBody;
    @NotNull
    private List<ProjectRelation> dependencies = new ArrayList<>();
    @SerializedName("game_versions")
    private List<String> gameVersions;
    /**
     * The release channel for this version
     * <p>
     * Allowed values: release, beta, alpha
     */
    @SerializedName("release_channel")
    @NotNull
    private String releaseChannel;
    @NotNull
    private List<String> loaders = new ArrayList<>();
    private boolean featured = true;
    /**
     * Allowed values: listed, archived, draft, unlisted, scheduled
     */
    private String status;
    /**
     * Allowed values: listed, archived, draft, unlisted
     */
    @SerializedName("requested_status")
    private String requestedStatus;
    @SerializedName("project_id")
    @NotNull
    private String projectId;
    /**
     * An array of the multipart field names of each file that goes with this version
     */
    @SerializedName("file_parts")
    private List<String> fileParts;
    /**
     * The multipart field name of the primary file
     */
    @SerializedName("primary_file")
    private String primaryFile;

    @SerializedName("file_types")
    @Nullable
    private List<String> fileTypes;

    public ModrinthFileData() {
    }

    public static ModrinthFileData create() {
        return new ModrinthFileData();
    }

    public static ModrinthFileData fromJson(String json) {
        return JsonParser.fromJson(json, ModrinthFileData.class);
    }

    public String name() {
        return name;
    }

    public ModrinthFileData name(String name) {
        this.name = name;
        return this;
    }

    public String versionNumber() {
        return versionNumber;
    }

    public ModrinthFileData versionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
        return this;
    }

    public String versionBody() {
        return versionBody;
    }

    public ModrinthFileData versionBody(String changelog) {
        this.versionBody = changelog;
        return this;
    }

    public List<ProjectRelation> dependencies() {
        return dependencies;
    }

    public ModrinthFileData dependencies(List<ProjectRelation> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public ModrinthFileData dependency(ProjectRelation dependency) {
        if (dependencies == null)
            dependencies = new ArrayList<>();
        for (ProjectRelation rel : dependencies)
            if (rel.getProjectID().equals(dependency.getProjectID())) return this;
        dependencies.add(dependency);
        return this;
    }

    public ModrinthFileData requiredDependency(String slug) {
        return dependency(ProjectRelation.createRequired(slug));
    }

    public ModrinthFileData optionalDependency(String slug) {
        return dependency(ProjectRelation.createOptional(slug));
    }

    public ModrinthFileData embeddedLibrary(String slug) {
        return dependency(ProjectRelation.createEmbedded(slug));
    }

    public ModrinthFileData incompatible(String slug) {
        return dependency(ProjectRelation.createIncompatible(slug));
    }

    public List<String> gameVersions() {
        return gameVersions;
    }

    public ModrinthFileData gameVersions(List<String> gameVersions) {
        this.gameVersions = gameVersions;
        return this;
    }

    public ModrinthFileData gameVersion(String gameVersion) {
        if (gameVersions == null) gameVersions = new ArrayList<>();
        if (!gameVersions.isEmpty() && gameVersions.contains(gameVersion)) return this;
        gameVersions.add(gameVersion);
        return this;
    }

    public ModrinthFileData gameVersion(MinecraftVersion gameVersion) {
        if (gameVersions == null) gameVersions = new ArrayList<>();
        if (!gameVersions.isEmpty() && gameVersions.contains(gameVersion.getVersion())) return this;
        gameVersions.add(gameVersion.getVersion());
        return this;
    }

    public String releaseChannel() {
        return releaseChannel;
    }

    public ModrinthFileData releaseChannel(ReleaseType type) {
        this.releaseChannel = type.getType();
        return this;
    }

    public ModrinthFileData release() {
        return releaseChannel(ReleaseType.Release);
    }

    public ModrinthFileData beta() {
        return releaseChannel(ReleaseType.Beta);
    }

    public ModrinthFileData alpha() {
        return releaseChannel(ReleaseType.Alpha);
    }

    public List<String> loaders() {
        return loaders;
    }

    public ModrinthFileData loaders(List<String> loaders) {
        this.loaders = loaders;
        return this;
    }

    public ModrinthFileData loader(String loader) {
        if (loaders == null) loaders = new ArrayList<>();
        if (!loaders.isEmpty() && loaders.contains(loader)) return this;
        loaders.add(loader);
        return this;
    }

    public ModrinthFileData loader(LauncherInfo info) {
        return loader(info.getId());
    }

    public boolean featured() {
        return featured;
    }

    public ModrinthFileData featured(boolean featured) {
        this.featured = featured;
        return this;
    }

    public String status() {
        return status;
    }

    public ModrinthFileData status(String status) {
        this.status = status;
        return this;
    }

    public ModrinthFileData status(RequestStatus type) {
        return status(type.getStatus());
    }

    public String requestedStatus() {
        return requestedStatus;
    }

    public ModrinthFileData requestedStatus(String requestedStatus) {
        this.requestedStatus = requestedStatus;
        return this;
    }

    public ModrinthFileData requestedStatus(RequestStatus type) {
        if (type.equals(RequestStatus.Scheduled)) return this;
        return requestedStatus(type.getStatus());
    }

    public String projectId() {
        return projectId;
    }

    public ModrinthFileData projectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public List<String> fileParts() {
        return fileParts;
    }

    public ModrinthFileData fileParts(List<String> fileParts) {
        this.fileParts = fileParts;
        return this;
    }

    public ModrinthFileData filePart(String filePart) {
        if (fileParts == null) fileParts = new ArrayList<>();
        if (!fileParts.isEmpty() && fileParts.contains(filePart)) return this;
        if (fileParts.isEmpty()) fileParts.add(filePart + "-primary");
        else {
            int i = fileParts.size() - 1;
            fileParts.add(filePart + "-" + i);
        }
        return this;
    }

    public ModrinthFileData filePart(File file) {
        return filePart(file.getName());
    }

    /*public String primaryFile() {
        return primaryFile;
    }

    public ModrinthFileData primaryFile(String primaryFile) {
        this.primaryFile = primaryFile + "-primary";
        return this;
    }

    public ModrinthFileData primaryFile(File primaryFile) {
        return primaryFile(primaryFile.getName());
    }*/

    public boolean isValid() {
        return projectId != null && !projectId.trim().isEmpty() ||
                versionNumber != null && !versionNumber.trim().isEmpty() ||
                gameVersions != null && !gameVersions.isEmpty() ||
                name != null && !name.trim().isEmpty() ||
                fileParts != null && !fileParts.isEmpty() ||
                primaryFile != null && !primaryFile.trim().isEmpty();
    }

    public String toJson() {
        return JsonParser.toJson(this);
    }

    @Override
    public String toString() {
        return "ModrinthFileData{" +
                "name='" + name + '\'' +
                ", versionNumber='" + versionNumber + '\'' +
                ", changelog='" + versionBody + '\'' +
                ", dependencies=" + dependencies +
                ", gameVersions=" + gameVersions +
                ", versionType='" + releaseChannel + '\'' +
                ", loaders=" + loaders +
                ", featured=" + featured +
                ", status='" + status + '\'' +
                ", requestedStatus='" + requestedStatus + '\'' +
                ", projectId='" + projectId + '\'' +
                ", fileParts=" + fileParts +
                ", primaryFile='" + primaryFile + '\'' +
                '}';
    }
}
