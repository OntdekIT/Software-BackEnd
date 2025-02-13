package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateMyAccountRequest(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        String email,
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$", message = "must be at least 8 characters long, contain at least one capital letter and one number")
        String password
) {
}
