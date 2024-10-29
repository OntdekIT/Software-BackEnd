package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String confirmPassword,
        Long stationCode,
        Long workshopCode
) {}
