package Ontdekstation013.ClimateChecker.features.admin.endpoint;

public class GrantUserAdminRequestDto {
    private String userId;
    private Boolean adminRights;

    public String getUserId() {
        return userId;
    }

    public Boolean getAdminRights() {
        return adminRights;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAdminRights(Boolean adminRights) {
        this.adminRights = adminRights;
    }
}
