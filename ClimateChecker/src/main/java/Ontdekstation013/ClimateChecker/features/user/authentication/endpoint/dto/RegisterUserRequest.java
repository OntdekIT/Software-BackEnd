package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

public record RegisterUserRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        Long stationCode,
        Long workshopCode
) {}