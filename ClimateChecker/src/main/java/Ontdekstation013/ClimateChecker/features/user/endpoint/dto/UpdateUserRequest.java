package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

import Ontdekstation013.ClimateChecker.features.user.UserRole;

public record UpdateUserRequest(
        UserRole role
) {
}
