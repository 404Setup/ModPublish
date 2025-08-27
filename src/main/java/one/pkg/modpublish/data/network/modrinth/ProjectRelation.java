package one.pkg.modpublish.data.network.modrinth;

import com.google.gson.annotations.SerializedName;
import one.pkg.modpublish.data.local.DependencyType;

@SuppressWarnings("unused")
public class ProjectRelation {
    @SerializedName("version_id")
    private String versionID;
    @SerializedName("project_id")
    private String projectID;
    @SerializedName("file_name")
    private String fileName;
    @SerializedName("dependency_type")
    private String type;

    public ProjectRelation() {
    }

    public ProjectRelation(String projectID, String type) {
        this.projectID = projectID;
        this.type = type;
    }

    public static ProjectRelation createRequired(String projectID) {
        return new ProjectRelation(projectID, DependencyType.REQUIRED.getModrinthName());
    }

    public static ProjectRelation createOptional(String projectID) {
        return new ProjectRelation(projectID, DependencyType.OPTIONAL.getModrinthName());
    }

    public static ProjectRelation createEmbedded(String projectID) {
        return new ProjectRelation(projectID, DependencyType.EMBEDDED.getModrinthName());
    }

    public static ProjectRelation createIncompatible(String projectID) {
        return new ProjectRelation(projectID, DependencyType.INCOMPATIBLE.getModrinthName());
    }

    public static ProjectRelation create(String projectID, DependencyType type) {
        return switch (type) {
            case EMBEDDED -> createEmbedded(projectID);
            case OPTIONAL -> createOptional(projectID);
            case REQUIRED -> createRequired(projectID);
            case INCOMPATIBLE -> createIncompatible(projectID);
        };
    }

    public String getVersionID() {
        return versionID;
    }

    public void setVersionID(String versionID) {
        this.versionID = versionID;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isValid() {
        return projectID != null && !projectID.trim().isEmpty() ||
                type != null && !type.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "ProjectRelation{" +
                ", projectID='" + projectID + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

}
