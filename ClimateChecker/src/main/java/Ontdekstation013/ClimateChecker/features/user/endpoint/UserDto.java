package Ontdekstation013.ClimateChecker.features.user.endpoint;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean isAdmin
) {}
