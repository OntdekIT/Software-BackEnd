package Ontdekstation013.ClimateChecker.features.station.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Invoer voor het handmatig toevoegen van een meetstation. Alleen deze velden
 * worden overgenomen; eigenaar, status en foutvlaggen zet de server zelf.
 */
public record AddStationRequest(
        @NotNull Long stationid,
        @NotBlank @Size(max = 255) String name,
        @NotNull Long registrationCode,
        Long location_locationid
) {}
