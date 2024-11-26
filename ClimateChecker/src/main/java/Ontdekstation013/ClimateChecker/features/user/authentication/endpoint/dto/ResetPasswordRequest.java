package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(

        @NotBlank
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$", message = "must be at least 8 characters long, contain at least one capital letter and one number")
        String password,

        @NotBlank
        String token
) {
}