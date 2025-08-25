package one.pkg.modpublish.data.network.curseforge;

import com.google.gson.annotations.SerializedName;

public class CurseforgePublishResult {
    /**
     * The uploaded file ID
     */
    @SerializedName("id")
    private Integer id;

    public CurseforgePublishResult() {
    }

    public CurseforgePublishResult(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    /**
     * Checks if the response is valid (contains a valid ID)
     *
     * @return true if ID is not null and greater than 0
     */
    public boolean isValid() {
        return id != null && id > 0;
    }

    /**
     * Checks if the upload was successful
     *
     * @return true if there is a valid ID
     */
    public boolean isSuccess() {
        return isValid();
    }

    /**
     * Gets the file ID as a string
     *
     * @return string representation of file ID, or empty string if ID is null
     */
    public String getIdAsString() {
        return id != null ? id.toString() : "";
    }

    /**
     * Creates a successful response object
     *
     * @param fileId the file ID
     * @return CurseforgePublishResult object
     */
    public static CurseforgePublishResult success(Integer fileId) {
        return new CurseforgePublishResult(fileId);
    }

    /**
     * Creates a failure response object
     *
     * @return CurseforgePublishResult object with null ID
     */
    public static CurseforgePublishResult failure() {
        return new CurseforgePublishResult();
    }

    @Override
    public String toString() {
        return "CurseforgePublishResult{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CurseforgePublishResult that = (CurseforgePublishResult) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}