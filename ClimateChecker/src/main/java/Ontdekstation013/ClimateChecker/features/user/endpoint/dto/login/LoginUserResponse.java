package Ontdekstation013.ClimateChecker.features.user.endpoint.dto.login;

public record LoginUserResponse(Long userId, String email, boolean isAdmin, String token) {
}
