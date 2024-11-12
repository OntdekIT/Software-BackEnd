package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

public record UpdateMyAccountRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {}
