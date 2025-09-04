package one.pkg.modpublish.data.local;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class LauncherInfo {
    public String n;    // name
    public String id;   // id
    public int cfid;    // curseforge id

    public LauncherInfo() {
    }

    public LauncherInfo(String name, String id, int cfid) {
        this.n = name;
        this.id = id;
        this.cfid = cfid;
    }
}