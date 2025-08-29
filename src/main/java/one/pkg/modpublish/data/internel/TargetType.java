package one.pkg.modpublish.data.internel;

import one.pkg.modpublish.api.API;
import one.pkg.modpublish.api.CurseForgeAPI;
import one.pkg.modpublish.api.GithubAPI;
import one.pkg.modpublish.api.GitlabAPI;
import one.pkg.modpublish.api.ModrinthAPI;

public enum TargetType {
    Modrinth(new ModrinthAPI()), CurseForge(new CurseForgeAPI()),
    Github(new GithubAPI()), Gitlab(new GitlabAPI());

    public final API api;

    TargetType(API api) {
        this.api = api;
    }
}
