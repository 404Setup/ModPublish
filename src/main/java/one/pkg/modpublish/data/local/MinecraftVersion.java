package one.pkg.modpublish.data.local;

@SuppressWarnings("unused")
public class MinecraftVersion {
    public String v;  // version
    public String t;  // type (release/snapshot)
    public int i; // id (curseforge)
    public String d;  // date

    public MinecraftVersion() {
    }

    public MinecraftVersion(String version, String type, int id, String date) {
        this.v = version;
        this.t = type;
        this.i = id;
        this.d = date;
    }

    public String getVersion() {
        return v;
    }

    public String getType() {
        return t;
    }

    public int getId() {
        return i;
    }

    public String getDate() {
        return d;
    }

    public boolean canReleaseToCurseForge() {
        return t.equals("release") && i > 0;
    }

    // Getter properties for easier access
    public String getV() {
        return v;
    }

    public String getT() {
        return t;
    }

    public int getI() {
        return i;
    }

    public String getD() {
        return d;
    }
}