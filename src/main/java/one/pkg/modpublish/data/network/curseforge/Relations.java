package one.pkg.modpublish.data.network.curseforge;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Relations {
    /**
     * Project relations list
     */
    @SerializedName("projects")
    private List<ProjectRelation> projects;

    public Relations() {
    }

    public Relations(List<ProjectRelation> projects) {
        this.projects = projects;
    }

    public List<ProjectRelation> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectRelation> projects) {
        this.projects = projects;
    }

    /**
     * Add a project relation
     *
     * @param relation Project relation to add
     */
    public void addProject(ProjectRelation relation) {
        if (projects != null) {
            projects.add(relation);
        }
    }

    /**
     * Check if there are any project relations
     *
     * @return true if there are project relations
     */
    public boolean hasProjects() {
        return projects != null && !projects.isEmpty();
    }

    /**
     * Get number of project relations
     *
     * @return Number of project relations
     */
    public int getProjectCount() {
        return projects != null ? projects.size() : 0;
    }

    @Override
    public String toString() {
        return "Relations{" +
                "projects=" + projects +
                '}';
    }

}