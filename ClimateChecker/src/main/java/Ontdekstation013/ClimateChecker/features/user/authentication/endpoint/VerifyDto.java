package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

public record VerifyDto(
        String email,
        String code
) {}
