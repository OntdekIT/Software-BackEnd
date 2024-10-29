package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

public record GrantUserAdminRequest(
        String userId,
        boolean hasAdminRights
) {}
