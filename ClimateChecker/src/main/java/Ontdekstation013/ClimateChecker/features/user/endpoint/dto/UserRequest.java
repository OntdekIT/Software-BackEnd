package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

public record UserRequest(
        String firstName,
        String lastName,
        String email
) {}
