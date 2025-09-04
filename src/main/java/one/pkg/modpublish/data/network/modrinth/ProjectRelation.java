package one.pkg.modpublish.data.network.modrinth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import one.pkg.modpublish.data.local.DependencyType;

@Data
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

    public boolean isValid() {
        return projectID != null && !projectID.trim().isEmpty() ||
                type != null && !type.trim().isEmpty();
    }
}
