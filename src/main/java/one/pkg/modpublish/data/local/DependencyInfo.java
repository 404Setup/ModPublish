package one.pkg.modpublish.data.local;

import lombok.Getter;
import lombok.Setter;
import one.pkg.modpublish.data.internel.ModInfo;

@Getter
@Setter
@SuppressWarnings("unused")
public class DependencyInfo {
    private String projectId;
    private ModInfo modrinthModInfo;
    private ModInfo curseforgeModInfo;
    private DependencyType type;
    private String customTitle;

    public DependencyInfo() {
    }

    public DependencyInfo(String projectId, DependencyType type, String customTitle) {
        this.projectId = projectId;
        this.type = type;
        this.customTitle = customTitle;
    }

}