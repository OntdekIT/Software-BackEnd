package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

public record RegisterDto(
        String firstName,
        String lastName,
        String email,
        String password,
        String confirmPassword,
        Long stationCode,
        Long workshopCode
) {}
