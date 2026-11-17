package Ontdekstation013.ClimateChecker.features.workshop.endpoint.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkshopRequest(
        @NotBlank String expirationDate
) {}
