package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

public record VerifyLoginRequest(
        String email,
        String code
) {}
