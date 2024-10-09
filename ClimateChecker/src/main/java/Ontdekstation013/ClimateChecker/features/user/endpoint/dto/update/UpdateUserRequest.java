package Ontdekstation013.ClimateChecker.features.user.endpoint.dto.update;

public record UpdateUserRequest(String firstName,
                                String lastName,
                                String email,
                                String newPassword) {
}