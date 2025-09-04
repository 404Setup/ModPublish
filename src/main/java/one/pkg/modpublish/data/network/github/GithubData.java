package one.pkg.modpublish.data.network.github;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import one.pkg.modpublish.data.internel.ReleaseChannel;
import one.pkg.modpublish.util.io.JsonParser;

@SuppressWarnings("unused")
@Data
@Builder(toBuilder = true)
public class GithubData {
    @SerializedName("tag_name")
    private String tagName;
    @SerializedName("target_commitish")
    private String targetCommitish;
    private String name;
    private String body;
    @Builder.Default
    private boolean draft = false;
    private boolean prerelease;
    @SerializedName("generate_release_notes")
    private boolean generateReleaseNotes;
    @SerializedName("make_latest")
    private String makeLatest;

    public static class GithubDataBuilder {
        public GithubDataBuilder releaseChannel(ReleaseChannel releaseChannel) {
            this.prerelease = !ReleaseChannel.Release.equals(releaseChannel);
            return this;
        }

        @Tolerate
        public GithubDataBuilder makeLatest(boolean makeLatest) {
            this.makeLatest = makeLatest ? "true" : "false";
            return this;
        }
    }

    public boolean makeLatest() {
        return "true".equals(makeLatest);
    }

    public String toJson() {
        return JsonParser.toJson(this);
    }
}
