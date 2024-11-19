package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

import Ontdekstation013.ClimateChecker.features.user.UserRole;

public record GetAllUsersRequest(
        Integer page,
        Integer pageSize,
        String firstName,
        String lastName,
        String email,
        UserRole role
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
