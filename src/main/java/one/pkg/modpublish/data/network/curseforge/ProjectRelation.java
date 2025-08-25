package one.pkg.modpublish.data.network.curseforge;

import com.google.gson.annotations.SerializedName;
import one.pkg.modpublish.data.local.DependencyType;

public class ProjectRelation {
    /**
     * Project slug identifier
     */
    @SerializedName("slug")
    private String slug;

    /**
     * Project ID - Optional, used for exact project matching
     */
    @SerializedName("projectID")
    private int projectID;

    /**
     * Dependency type
     * Possible values: embeddedLibrary, incompatible, optionalDependency, requiredDependency, tool
     */
    @SerializedName("type")
    private String type;

    public ProjectRelation() {
    }

    public ProjectRelation(String slug, String type) {
        this.slug = slug;
        this.type = type;
    }

    public ProjectRelation(String slug, int projectID, String type) {
        this.slug = slug;
        this.projectID = projectID;
        this.type = type;
    }

    /**
     * Create required dependency relation
     *
     * @param slug Project slug
     * @return ProjectRelation object
     */
    public static ProjectRelation createRequired(String slug) {
        return new ProjectRelation(slug, DependencyType.REQUIRED.getCurseforgeName());
    }

    public static ProjectRelation createRequired(String slug, int projectID) {
        return new ProjectRelation(slug, projectID, DependencyType.REQUIRED.getCurseforgeName());
    }

    /**
     * Create optional dependency relation
     *
     * @param slug Project slug
     * @return ProjectRelation object
     */
    public static ProjectRelation createOptional(String slug) {
        return new ProjectRelation(slug, DependencyType.OPTIONAL.getCurseforgeName());
    }

    public static ProjectRelation createOptional(String slug, int projectID) {
        return new ProjectRelation(slug, projectID, DependencyType.OPTIONAL.getCurseforgeName());
    }

    /**
     * Create embedded library relation
     *
     * @param slug Project slug
     * @return ProjectRelation object
     */
    public static ProjectRelation createEmbedded(String slug) {
        return new ProjectRelation(slug, DependencyType.EMBEDDED.getCurseforgeName());
    }

    public static ProjectRelation createEmbedded(String slug, int projectID) {
        return new ProjectRelation(slug, projectID, DependencyType.EMBEDDED.getCurseforgeName());
    }

    /**
     * Create incompatible relation
     *
     * @param slug Project slug
     * @return ProjectRelation object
     */
    public static ProjectRelation createIncompatible(String slug) {
        return new ProjectRelation(slug, DependencyType.INCOMPATIBLE.getCurseforgeName());
    }

    public static ProjectRelation createIncompatible(String slug, int projectID) {
        return new ProjectRelation(slug, projectID, DependencyType.INCOMPATIBLE.getCurseforgeName());
    }

    public static ProjectRelation create(String slug, DependencyType type) {
        return switch (type) {
            case EMBEDDED -> createEmbedded(slug);
            case OPTIONAL -> createOptional(slug);
            case REQUIRED -> createRequired(slug);
            case INCOMPATIBLE -> createIncompatible(slug);
        };
    }

    public static ProjectRelation create(String slug, int projectID, DependencyType type) {
        return switch (type) {
            case EMBEDDED -> createEmbedded(slug, projectID);
            case OPTIONAL -> createOptional(slug, projectID);
            case REQUIRED -> createRequired(slug, projectID);
            case INCOMPATIBLE -> createIncompatible(slug, projectID);
        };
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Check if project ID exists
     *
     * @return true if project ID exists
     */
    public boolean hasProjectID() {
        return projectID > 1;
    }

    /**
     * Validate if fields are valid
     *
     * @return true if required fields are set
     */
    public boolean isValid() {
        return slug != null && !slug.trim().isEmpty() &&
                hasProjectID() ||
                type != null && !type.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "ProjectRelation{" +
                "slug='" + slug + '\'' +
                ", projectID='" + projectID + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

}