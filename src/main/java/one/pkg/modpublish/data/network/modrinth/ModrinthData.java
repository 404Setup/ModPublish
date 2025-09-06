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

package one.pkg.modpublish.data.network.modrinth;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Tolerate;
import one.pkg.modpublish.data.internel.ReleaseChannel;
import one.pkg.modpublish.data.internel.RequestStatus;
import one.pkg.modpublish.data.local.LauncherInfo;
import one.pkg.modpublish.data.local.MinecraftVersion;
import one.pkg.modpublish.util.io.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment: Why are many parameters here inconsistent with the API docs?
 */
@SuppressWarnings("unused")
@Getter
@Builder(toBuilder = true)
public class ModrinthData {
    @SerializedName("version_title")
    @NotNull
    private String name;
    @SerializedName("version_number")
    @NotNull
    private String versionNumber;
    @SerializedName("version_body")
    private String versionBody;
    @NotNull
    private List<ProjectRelation> dependencies;
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
    private List<String> loaders;
    @Builder.Default
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

    public static ModrinthData fromJson(String json) {
        return JsonParser.fromJson(json, ModrinthData.class);
    }

    public boolean isValid() {
        return !projectId.trim().isEmpty() || !versionNumber.trim().isEmpty() || gameVersions != null
                && !gameVersions.isEmpty() || !name.trim().isEmpty() || fileParts != null &&
                !fileParts.isEmpty() || primaryFile != null && !primaryFile.trim().isEmpty();
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

    public String toJson() {
        return JsonParser.toJson(this);
    }

    public static class ModrinthDataBuilder {
        private List<String> loaders = new ArrayList<>();
        private List<String> fileParts = new ArrayList<>();
        private List<ProjectRelation> dependencies = new ArrayList<>();

        public ModrinthDataBuilder dependency(ProjectRelation dependency) {
            if (this.dependencies == null)
                this.dependencies = new ArrayList<>();
            for (ProjectRelation rel : this.dependencies)
                if (rel.getProjectID().equals(dependency.getProjectID())) return this;
            this.dependencies.add(dependency);
            return this;
        }

        public ModrinthDataBuilder requiredDependency(String slug) {
            return dependency(ProjectRelation.createRequired(slug));
        }

        public ModrinthDataBuilder optionalDependency(String slug) {
            return dependency(ProjectRelation.createOptional(slug));
        }

        public ModrinthDataBuilder embeddedLibrary(String slug) {
            return dependency(ProjectRelation.createEmbedded(slug));
        }

        public ModrinthDataBuilder incompatible(String slug) {
            return dependency(ProjectRelation.createIncompatible(slug));
        }

        public ModrinthDataBuilder gameVersion(String gameVersion) {
            if (this.gameVersions == null) this.gameVersions = new ArrayList<>();
            if (!this.gameVersions.isEmpty() && this.gameVersions.contains(gameVersion)) return this;
            this.gameVersions.add(gameVersion);
            return this;
        }

        public ModrinthDataBuilder gameVersion(MinecraftVersion gameVersion) {
            if (this.gameVersions == null) this.gameVersions = new ArrayList<>();
            if (!this.gameVersions.isEmpty() && this.gameVersions.contains(gameVersion.getVersion())) return this;
            this.gameVersions.add(gameVersion.getVersion());
            return this;
        }

        @Tolerate
        public ModrinthDataBuilder releaseChannel(ReleaseChannel type) {
            this.releaseChannel = type.getType();
            return this;
        }

        public ModrinthDataBuilder release() {
            return releaseChannel(ReleaseChannel.Release);
        }

        public ModrinthDataBuilder beta() {
            return releaseChannel(ReleaseChannel.Beta);
        }

        public ModrinthDataBuilder alpha() {
            return releaseChannel(ReleaseChannel.Alpha);
        }

        public ModrinthDataBuilder loader(String loader) {
            if (this.loaders == null) this.loaders = new ArrayList<>();
            if (!this.loaders.isEmpty() && this.loaders.contains(loader)) return this;
            this.loaders.add(loader);
            return this;
        }

        public ModrinthDataBuilder loader(LauncherInfo info) {
            return loader(info.getId());
        }

        @Tolerate
        public ModrinthDataBuilder status(RequestStatus type) {
            this.status = type.getStatus();
            return this;
        }

        @Tolerate
        public ModrinthDataBuilder requestedStatus(RequestStatus type) {
            if (type.equals(RequestStatus.Scheduled)) return this;
            this.requestedStatus = type.getStatus();
            return this;
        }

        public ModrinthDataBuilder filePart(String filePart) {
            if (this.fileParts == null) this.fileParts = new ArrayList<>();
            if (!fileParts.isEmpty() && this.fileParts.contains(filePart)) return this;
            if (this.fileParts.isEmpty()) this.fileParts.add(filePart + "-primary");
            else {
                int i = this.fileParts.size() - 1;
                this.fileParts.add(filePart + "-" + i);
            }
            return this;
        }

        public ModrinthDataBuilder filePart(File file) {
            return filePart(file.getName());
        }
    }
}
