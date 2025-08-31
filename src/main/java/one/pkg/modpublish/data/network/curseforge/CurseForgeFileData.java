package one.pkg.modpublish.data.network.curseforge;

import com.google.gson.annotations.SerializedName;
import one.pkg.modpublish.data.internel.ReleaseChannel;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.util.io.JsonParser;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class CurseForgeFileData {

    /**
     * Changelog - A string describing changes, can be HTML or Markdown format
     */
    @SerializedName("changelog")
    private String changelog;
    /**
     * Changelog type - Optional, defaults to text
     * Valid values: text, html, markdown
     */
    @SerializedName("changelogType")
    private String changelogType;
    /**
     * Display name - Optional, friendly name shown on website
     */
    @SerializedName("displayName")
    private String displayName;
    /**
     * Parent file ID - Optional, parent file of this file
     */
    @SerializedName("parentFileID")
    private Integer parentFileID;
    /**
     * List of supported game versions
     * Not supported if parentFileID is provided
     */
    @SerializedName("gameVersions")
    private List<Integer> gameVersions;
    /**
     * Release type: one of alpha, beta, release
     */
    @SerializedName("releaseType")
    private String releaseType;
    /**
     * Whether marked for manual release - Optional
     * If true, file won't publish immediately after approval, can choose when to publish
     */
    @SerializedName("isMarkedForManualRelease")
    private Boolean isMarkedForManualRelease;
    /**
     * Project relations - Optional
     * Array of project relationships by slug and dependency type
     */
    @SerializedName("relations")
    private Relations relations;

    public CurseForgeFileData() {
    }

    public CurseForgeFileData(String changelog, String releaseType) {
        this.changelog = changelog;
        this.releaseType = releaseType;
        this.changelogType = "text";
    }

    public static CurseForgeFileData create() {
        return new CurseForgeFileData();
    }

    /**
     * Creates a basic file data object
     *
     * @param changelog    The changelog
     * @param releaseType  The release type
     * @param gameVersions Supported game versions
     * @return CurseForgeFileData object
     */
    public static CurseForgeFileData createBasic(String changelog, String releaseType, List<Integer> gameVersions) {
        CurseForgeFileData data = new CurseForgeFileData(changelog, releaseType);
        data.gameVersions(gameVersions);
        return data;
    }

    public String changelog() {
        return changelog;
    }

    public CurseForgeFileData changelog(String changelog) {
        this.changelog = changelog;
        this.changelogType = "text";
        return this;
    }

    public String changelogType() {
        return changelogType;
    }

    public CurseForgeFileData changelogType(String changelogType) {
        this.changelogType = changelogType;
        return this;
    }

    public String displayName() {
        return displayName;
    }

    public CurseForgeFileData displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Integer parentFileID() {
        return parentFileID;
    }

    public CurseForgeFileData parentFileID(Integer parentFileID) {
        this.parentFileID = parentFileID;
        return this;
    }

    public List<Integer> gameVersions() {
        return gameVersions;
    }

    public CurseForgeFileData gameVersions(List<Integer> gameVersions) {
        this.gameVersions = gameVersions;
        return this;
    }

    public CurseForgeFileData gameVersion(MinecraftVersion version) {
       return version.canReleaseToCurseForge() ? gameVersion(version.i) : this;
    }

    public CurseForgeFileData gameVersion(Integer gameVersion) {
        if (gameVersions == null) gameVersions = new ArrayList<>();
        if (!gameVersions.isEmpty() && gameVersions.contains(gameVersion)) return this;
        gameVersions.add(gameVersion);
        return this;
    }

    public CurseForgeFileData alpha() {
        return releaseType(ReleaseChannel.Alpha);
    }

    public CurseForgeFileData beta() {
        return releaseType(ReleaseChannel.Beta);
    }

    public CurseForgeFileData release() {
        return releaseType(ReleaseChannel.Release);
    }

    public String releaseType() {
        return releaseType;
    }

    public CurseForgeFileData releaseType(ReleaseChannel releaseChannel) {
        this.releaseType = releaseChannel.getType();
        return this;
    }

    public CurseForgeFileData dependency(ProjectRelation relation) {
        if (this.relations == null) this.relations = new Relations();
        if (this.relations.getProjects() == null) this.relations.setProjects(new ArrayList<>());
        this.relations.addProject(relation);
        return this;
    }

    public CurseForgeFileData requiredDependency(String slug) {
        return dependency(ProjectRelation.createRequired(slug));
    }

    public CurseForgeFileData requiredDependency(String slug, int projectID) {
        return dependency(ProjectRelation.createRequired(slug, projectID));
    }

    public CurseForgeFileData optionalDependency(String slug) {
        return dependency(ProjectRelation.createOptional(slug));
    }

    public CurseForgeFileData optionalDependency(String slug, int projectID) {
        return dependency(ProjectRelation.createOptional(slug, projectID));
    }

    public CurseForgeFileData embeddedLibrary(String slug) {
        return dependency(ProjectRelation.createEmbedded(slug));
    }

    public CurseForgeFileData embeddedLibrary(String slug, int projectID) {
        return dependency(ProjectRelation.createEmbedded(slug, projectID));
    }

    public CurseForgeFileData incompatible(String slug) {
        return dependency(ProjectRelation.createIncompatible(slug));
    }

    public CurseForgeFileData incompatible(String slug, int projectID) {
        return dependency(ProjectRelation.createIncompatible(slug, projectID));
    }

    public boolean isMarkedForManualRelease() {
        return isMarkedForManualRelease;
    }

    public CurseForgeFileData isMarkedForManualRelease(boolean isMarkedForManualRelease) {
        this.isMarkedForManualRelease = isMarkedForManualRelease;
        return this;
    }

    public Relations dependencies() {
        return relations;
    }

    public CurseForgeFileData dependencies(Relations relations) {
        this.relations = relations;
        return this;
    }

    /**
     * Sets HTML format changelog
     *
     * @param htmlChangelog Changelog in HTML format
     */
    public CurseForgeFileData htmlChangelog(String htmlChangelog) {
        this.changelog = htmlChangelog;
        this.changelogType = "html";
        return this;
    }

    /**
     * Sets Markdown format changelog
     *
     * @param markdownChangelog Changelog in Markdown format
     */
    public CurseForgeFileData markdownChangelog(String markdownChangelog) {
        this.changelog = markdownChangelog;
        this.changelogType = "markdown";
        return this;
    }

    /**
     * Checks if there is a parent file
     *
     * @return true if there is a parent file
     */
    public boolean parentFile() {
        return parentFileID != null;
    }

    /**
     * Checks if marked for manual release
     *
     * @return true if marked for manual release
     */
    public boolean manualRelease() {
        return isMarkedForManualRelease;
    }

    /**
     * Validates that required fields are set
     *
     * @return true if all required fields are set
     */
    public boolean isValid() {
        return changelog != null && !changelog.trim().isEmpty() &&
                releaseType != null && !releaseType.trim().isEmpty() &&
                (parentFile() || (gameVersions != null && !gameVersions.isEmpty()));
    }

    public String toJson() {
        return JsonParser.toJson(this);
    }

    @Override
    public String toString() {
        return "CurseForgeFileData{" +
                "changelog='" + changelog + '\'' +
                ", changelogType='" + changelogType + '\'' +
                ", displayName='" + displayName + '\'' +
                ", parentFileID=" + parentFileID +
                ", gameVersions=" + gameVersions +
                ", releaseChannel='" + releaseType + '\'' +
                ", isMarkedForManualRelease=" + isMarkedForManualRelease +
                ", relations=" + relations +
                '}';
    }
}