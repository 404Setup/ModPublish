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

package one.pkg.modpublish.data.network.github;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Tolerate;
import one.pkg.modpublish.data.internal.ReleaseChannel;
import one.pkg.modpublish.util.io.JsonParser;

@SuppressWarnings("unused")
@Getter
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

    public boolean makeLatest() {
        return "true".equals(makeLatest);
    }

    public String toJson() {
        return JsonParser.toJson(this);
    }

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
}
