package Ontdekstation013.ClimateChecker.features.user.endpoint;

public record GrantUserAdminRequest(
        String userId,
        Boolean adminRights
) {}
