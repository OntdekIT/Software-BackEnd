package Ontdekstation013.ClimateChecker.features.station.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TransferStationRequest(
        @NotBlank
        @Email
        String newOwnerEmail
) {
}
