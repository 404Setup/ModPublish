package one.pkg.modpublish.data.internel;

import one.pkg.modpublish.api.API;
import one.pkg.modpublish.api.CurseforgeAPI;
import one.pkg.modpublish.api.GithubAPI;
import one.pkg.modpublish.api.GitlabAPI;
import one.pkg.modpublish.api.ModrinthAPI;

public enum TargetType {
    Modrinth(new ModrinthAPI()), Curseforge(new CurseforgeAPI()),
    Github(new GithubAPI()), Gitlab(new GitlabAPI());

    private final API api;

    TargetType(API api) {
        this.api = api;
    }

    public API getApi() {
        return api;
    }
}
