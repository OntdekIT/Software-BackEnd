package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMyAccountRequest(
        String password,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        String email
) {
    public boolean isPasswordValid() {
        return password == null || password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$");
    }
}
