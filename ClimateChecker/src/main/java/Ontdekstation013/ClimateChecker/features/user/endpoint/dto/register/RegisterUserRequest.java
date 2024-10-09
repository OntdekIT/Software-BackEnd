package Ontdekstation013.ClimateChecker.features.user.endpoint.dto.register;

public record RegisterUserRequest(String firstName,
                                  String lastName,
                                  String email,
                                  String password,
                                  Long code,
                                  Long stationId) {
}
