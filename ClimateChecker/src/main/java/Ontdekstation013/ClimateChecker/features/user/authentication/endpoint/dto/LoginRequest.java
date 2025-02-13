package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

public record LoginRequest(
        String email,
        String password
) {}
