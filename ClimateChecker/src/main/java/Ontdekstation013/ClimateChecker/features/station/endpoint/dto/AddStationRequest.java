package Ontdekstation013.ClimateChecker.features.station.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Invoer voor het handmatig toevoegen van een meetstation. Alleen deze velden
 * worden overgenomen; eigenaar, status en foutvlaggen zet de server zelf.
 * <p>
 * {@code stationid} is optioneel en valt terug op de registratiecode (bij
 * Meet je stad is die gelijk aan het station-id). De naam is optioneel.
 */
public record AddStationRequest(
        Long stationid,
        @Size(max = 255) String name,
        @NotNull Long registrationCode,
        Long location_locationid
) {}
