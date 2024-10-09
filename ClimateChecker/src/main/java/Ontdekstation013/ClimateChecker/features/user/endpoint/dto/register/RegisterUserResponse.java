package Ontdekstation013.ClimateChecker.features.user.endpoint.dto.register;

public record RegisterUserResponse(Long userId,
                                   String firstName,
                                   String lastName,
                                   String email,
                                   boolean isAdmin) {
}
