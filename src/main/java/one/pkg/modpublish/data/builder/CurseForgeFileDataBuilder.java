package one.pkg.modpublish.data.builder;

import one.pkg.modpublish.data.network.curseforge.CurseForgeFileData;
import one.pkg.modpublish.data.network.curseforge.ProjectRelation;
import one.pkg.modpublish.data.network.curseforge.Relations;

import java.util.ArrayList;
import java.util.List;

public class CurseForgeFileDataBuilder {
    private final CurseForgeFileData data;

    private CurseForgeFileDataBuilder() {
        this.data = new CurseForgeFileData();
    }

    public static CurseForgeFileDataBuilder create() {
        return new CurseForgeFileDataBuilder();
    }

    public CurseForgeFileDataBuilder changelog(String changelog) {
        data.setChangelog(changelog);
        data.setChangelogType("text");
        return this;
    }

    public CurseForgeFileDataBuilder htmlChangelog(String htmlChangelog) {
        data.setHtmlChangelog(htmlChangelog);
        return this;
    }

    public CurseForgeFileDataBuilder markdownChangelog(String markdownChangelog) {
        data.setMarkdownChangelog(markdownChangelog);
        return this;
    }

    public CurseForgeFileDataBuilder displayName(String displayName) {
        data.setDisplayName(displayName);
        return this;
    }

    public CurseForgeFileDataBuilder parentFileID(Integer parentFileID) {
        data.setParentFileID(parentFileID);
        return this;
    }

    public CurseForgeFileDataBuilder gameVersions(List<Integer> gameVersions) {
        data.setGameVersions(gameVersions);
        return this;
    }

    public CurseForgeFileDataBuilder addGameVersion(Integer gameVersion) {
        if (data.getGameVersions() == null) {
            data.setGameVersions(new ArrayList<>());
        }
        data.getGameVersions().add(gameVersion);
        return this;
    }

    public CurseForgeFileDataBuilder alpha() {
        data.setReleaseType("alpha");
        return this;
    }

    public CurseForgeFileDataBuilder beta() {
        data.setReleaseType("beta");
        return this;
    }

    public CurseForgeFileDataBuilder release() {
        data.setReleaseType("release");
        return this;
    }

    public CurseForgeFileDataBuilder releaseType(String releaseType) {
        data.setReleaseType(releaseType);
        return this;
    }

    public CurseForgeFileDataBuilder manualRelease() {
        data.setIsMarkedForManualRelease(true);
        return this;
    }

    public CurseForgeFileDataBuilder manualRelease(boolean manual) {
        data.setIsMarkedForManualRelease(manual);
        return this;
    }

    public CurseForgeFileDataBuilder requiredDependency(String slug) {
        return addRelation(ProjectRelation.createRequired(slug));
    }

    public CurseForgeFileDataBuilder requiredDependency(String slug, int projectID) {
        return addRelation(ProjectRelation.createRequired(slug, projectID));
    }

    public CurseForgeFileDataBuilder optionalDependency(String slug) {
        return addRelation(ProjectRelation.createOptional(slug));
    }

    public CurseForgeFileDataBuilder optionalDependency(String slug, int projectID) {
        return addRelation(ProjectRelation.createOptional(slug, projectID));
    }

    public CurseForgeFileDataBuilder embeddedLibrary(String slug) {
        return addRelation(ProjectRelation.createEmbedded(slug));
    }

    public CurseForgeFileDataBuilder embeddedLibrary(String slug, int projectID) {
        return addRelation(ProjectRelation.createEmbedded(slug, projectID));
    }

    public CurseForgeFileDataBuilder incompatible(String slug) {
        return addRelation(ProjectRelation.createIncompatible(slug));
    }

    public CurseForgeFileDataBuilder incompatible(String slug, int projectID) {
        return addRelation(ProjectRelation.createIncompatible(slug, projectID));
    }

    public CurseForgeFileDataBuilder addRelation(ProjectRelation relation) {
        if (data.getRelations() == null) {
            data.setRelations(new Relations());
        }
        if (data.getRelations().getProjects() == null) {
            data.getRelations().setProjects(new ArrayList<>());
        }
        data.getRelations().addProject(relation);
        return this;
    }

    public CurseForgeFileData build() {
        if (!data.isValid()) {
            throw new IllegalStateException("Missing required fields: changelog, releaseType and gameVersions");
        }
        return data;
    }

    public CurseForgeFileData buildUnchecked() {
        return data;
    }

}
