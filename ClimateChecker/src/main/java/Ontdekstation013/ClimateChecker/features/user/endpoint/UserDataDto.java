package Ontdekstation013.ClimateChecker.features.user.endpoint;

public record UserDataDto(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String email,
        boolean isAdmin
) {}
