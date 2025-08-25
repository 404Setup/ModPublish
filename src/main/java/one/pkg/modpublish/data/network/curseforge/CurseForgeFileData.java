package one.pkg.modpublish.data.network.curseforge;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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
        data.setGameVersions(gameVersions);
        return data;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public String getChangelogType() {
        return changelogType;
    }

    public void setChangelogType(String changelogType) {
        this.changelogType = changelogType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getParentFileID() {
        return parentFileID;
    }

    public void setParentFileID(Integer parentFileID) {
        this.parentFileID = parentFileID;
    }

    public List<Integer> getGameVersions() {
        return gameVersions;
    }

    public void setGameVersions(List<Integer> gameVersions) {
        this.gameVersions = gameVersions;
    }

    public void addGameVersion(Integer gameVersion) {
        if (gameVersions == null)
            gameVersions = new ArrayList<>();
        if (!gameVersions.isEmpty() && gameVersions.contains(gameVersion)) return;
        gameVersions.add(gameVersion);
    }

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public Boolean getIsMarkedForManualRelease() {
        return isMarkedForManualRelease;
    }

    public void setIsMarkedForManualRelease(Boolean isMarkedForManualRelease) {
        this.isMarkedForManualRelease = isMarkedForManualRelease;
    }

    public Relations getRelations() {
        return relations;
    }

    public void setRelations(Relations relations) {
        this.relations = relations;
    }

    /**
     * Sets HTML format changelog
     *
     * @param htmlChangelog Changelog in HTML format
     */
    public void setHtmlChangelog(String htmlChangelog) {
        this.changelog = htmlChangelog;
        this.changelogType = "html";
    }

    /**
     * Sets Markdown format changelog
     *
     * @param markdownChangelog Changelog in Markdown format
     */
    public void setMarkdownChangelog(String markdownChangelog) {
        this.changelog = markdownChangelog;
        this.changelogType = "markdown";
    }

    /**
     * Checks if there is a parent file
     *
     * @return true if there is a parent file
     */
    public boolean hasParentFile() {
        return parentFileID != null;
    }

    /**
     * Checks if marked for manual release
     *
     * @return true if marked for manual release
     */
    public boolean isManualRelease() {
        return Boolean.TRUE.equals(isMarkedForManualRelease);
    }

    /**
     * Validates that required fields are set
     *
     * @return true if all required fields are set
     */
    public boolean isValid() {
        return changelog != null && !changelog.trim().isEmpty() &&
                releaseType != null && !releaseType.trim().isEmpty() &&
                (hasParentFile() || (gameVersions != null && !gameVersions.isEmpty()));
    }

    @Override
    public String toString() {
        return "CurseForgeFileData{" +
                "changelog='" + changelog + '\'' +
                ", changelogType='" + changelogType + '\'' +
                ", displayName='" + displayName + '\'' +
                ", parentFileID=" + parentFileID +
                ", gameVersions=" + gameVersions +
                ", releaseType='" + releaseType + '\'' +
                ", isMarkedForManualRelease=" + isMarkedForManualRelease +
                ", relations=" + relations +
                '}';
    }
}