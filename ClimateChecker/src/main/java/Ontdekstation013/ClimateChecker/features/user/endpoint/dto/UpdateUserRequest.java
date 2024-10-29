package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

public record EditUserDto(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String email,
        String password
) {}
