package Ontdekstation013.ClimateChecker.features.station.endpoint.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStationRequest(
        @NotBlank
        String name,
        @NotBlank
        Boolean is_public
) {
}

