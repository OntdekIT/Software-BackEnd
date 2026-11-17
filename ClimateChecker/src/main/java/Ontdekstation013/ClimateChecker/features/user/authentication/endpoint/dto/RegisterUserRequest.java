package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

import Ontdekstation013.ClimateChecker.features.user.PasswordRules;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterUserRequest(
        String firstName,
        String lastName,
        String email,

        @NotBlank
        @Pattern(regexp = PasswordRules.PATTERN, message = PasswordRules.MESSAGE)
        String password,

        Long stationCode,
        Long workshopCode
) {}
