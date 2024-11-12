package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

public record GetAllUsersRequest(
        Integer page,
        Integer pageSize,
        String firstName,
        String lastName,
        String email,
        Boolean isAdmin
) {
    public GetAllUsersRequest {
        if (page == null) {
            page = 0;
        }

        if (pageSize == null) {
            pageSize = 20;
        }
    }
}
