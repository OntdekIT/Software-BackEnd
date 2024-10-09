package Ontdekstation013.ClimateChecker.features.user.endpoint.dto.update;

public record UpdateUserResponse(Long userId,
                                 String firstName,
                                 String lastName,
                                 String email) {
}
