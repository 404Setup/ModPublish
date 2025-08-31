package one.pkg.modpublish.data.network.github;

import com.google.gson.annotations.SerializedName;
import one.pkg.modpublish.data.internel.ReleaseChannel;
import one.pkg.modpublish.util.JsonParser;

@SuppressWarnings("unused")
public class GithubData {
    @SerializedName("tag_name")
    private String tagName;
    @SerializedName("target_commitish")
    private String targetCommitish;
    private String name;
    private String body;
    private boolean draft = false;
    private boolean prerelease;
    @SerializedName("generate_release_notes")
    private boolean generateReleaseNotes;
    @SerializedName("make_latest")
    private String makeLatest;

    public GithubData() {
    }

    public static GithubData create() {
        return new GithubData();
    }

    public String tagName() {
        return tagName;
    }

    public GithubData tagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public String targetCommitish() {
        return targetCommitish;
    }

    public GithubData targetCommitish(String targetCommitish) {
        this.targetCommitish = targetCommitish;
        return this;
    }

    public String name() {
        return name;
    }

    public GithubData name(String name) {
        this.name = name;
        return this;
    }

    public String body() {
        return body;
    }

    public GithubData body(String body) {
        this.body = body;
        return this;
    }

    public boolean draft() {
        return draft;
    }

    public GithubData draft(boolean draft) {
        this.draft = draft;
        return this;
    }

    public boolean prerelease() {
        return prerelease;
    }

    public GithubData prerelease(boolean prerelease) {
        this.prerelease = prerelease;
        return this;
    }

    public GithubData releaseChannel(ReleaseChannel releaseChannel) {
        this.prerelease = releaseChannel != ReleaseChannel.Release;
        return this;
    }

    public boolean generateReleaseNotes() {
        return generateReleaseNotes;
    }

    public GithubData generateReleaseNotes(boolean generateReleaseNotes) {
        this.generateReleaseNotes = generateReleaseNotes;
        return this;
    }

    public boolean makeLatest() {
        return makeLatest.equals("true");
    }

    public GithubData makeLatest(boolean makeLatest) {
        this.makeLatest = makeLatest ? "true" : "false";
        return this;
    }

    public String toJson() {
        return JsonParser.toJson(this);
    }

    @Override
    public String toString() {
        return "GithubData{" +
                "tagName='" + tagName + '\'' +
                ", targetCommitish='" + targetCommitish + '\'' +
                ", name='" + name + '\'' +
                ", body='" + body + '\'' +
                ", draft=" + draft +
                ", prerelease=" + prerelease +
                ", generateReleaseNotes=" + generateReleaseNotes +
                ", makeLatest=" + makeLatest +
                '}';
    }
}
