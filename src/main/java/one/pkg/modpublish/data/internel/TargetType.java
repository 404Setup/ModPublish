package one.pkg.modpublish.data.internel;

import one.pkg.modpublish.api.*;

public enum TargetType {
    Modrinth(new ModrinthAPI()), CurseForge(new CurseForgeAPI()),
    Github(new GithubAPI()), Gitlab(new GitlabAPI());

    public final API api;

    TargetType(API api) {
        this.api = api;
    }
}
