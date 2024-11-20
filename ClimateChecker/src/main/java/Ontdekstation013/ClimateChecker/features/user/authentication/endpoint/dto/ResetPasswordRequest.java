package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(

        @NotBlank
        String email,

        @NotBlank
        String password,

        @NotBlank
        String token
) {
}
