package one.pkg.modpublish.data.internel;

public enum RequestStatus {
    Listed("listed"), Archived("archived"), Draft("draft"), Unlisted("unlisted"), Scheduled("scheduled");

    private final String status;

    RequestStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
