package Ontdekstation013.ClimateChecker.features.measurement;

import java.time.Instant;

/** Platte weergave van een meetrij uit de database, zonder het station te laden. */
public record MeasurementRow(
        Long id,
        Long stationId,
        Instant timestamp,
        float latitude,
        float longitude,
        Float temperature,
        Float humidity,
        Float pm25,
        Float pm10,
        boolean locationInherited) {
}
