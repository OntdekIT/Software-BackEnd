package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

public record UpdateUserRequest(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String email,
        String password
) {}
