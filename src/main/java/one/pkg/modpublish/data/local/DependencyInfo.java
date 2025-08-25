package one.pkg.modpublish.data.local;

import one.pkg.modpublish.data.internel.ModInfo;
import org.jetbrains.annotations.Nullable;

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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public @Nullable ModInfo getModrinthInfo() {
        return modrinthModInfo;
    }

    public void setModrinthInfo(ModInfo info) {
        this.modrinthModInfo = info;
    }

    public @Nullable ModInfo getCurseforgeInfo() {
        return curseforgeModInfo;
    }

    public void setCurseforgeInfo(ModInfo info) {
        this.curseforgeModInfo = info;
    }

    public DependencyType getType() {
        return type;
    }

    public void setType(DependencyType type) {
        this.type = type;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }
}