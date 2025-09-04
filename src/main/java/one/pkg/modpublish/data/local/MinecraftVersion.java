package one.pkg.modpublish.data.local;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class MinecraftVersion {
    // Getter properties for easier access
    @SerializedName("v")
    public String version;  // version
    @SerializedName("t")
    public String type;  // type (release/snapshot)
    @SerializedName("i")
    public int id; // id (curseforge)
    @SerializedName("d")
    public String date;  // date

    public MinecraftVersion() {
    }

    public MinecraftVersion(String version, String type, int id, String date) {
        this.version = version;
        this.type = type;
        this.id = id;
        this.date = date;
    }

    public boolean canReleaseToCurseForge() {
        return type.equals("release") && id > 0;
    }

}