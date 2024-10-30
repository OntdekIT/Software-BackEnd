package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

public record GetAllUsersRequest(
        Integer page,
        String firstName,
        String lastName,
        String email,
        Boolean isAdmin
) {}
