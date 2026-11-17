package Ontdekstation013.ClimateChecker.snapshot;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * De geconsolideerde, vooraf berekende staat van het netwerk (GET /api/snapshot). Alle tijdstippen
 * zijn ISO-8601 in UTC. Gemiddelden zijn null als er geen actuele waarde is, nooit NaN en nooit een
 * oude waarde. Dit formaat is bewust stabiel: velden worden alleen toegevoegd, niet hernoemd.
 */
@Schema(description = "Vooraf berekende staat van het meetnetwerk")
public record Overview(
        int schemaVersion,
        String generatedAt,
        @Schema(description = "Tijdstip van de laatste geslaagde ingest (ISO-8601 UTC)") String dataAsOf,
        @Schema(description = "Metingen ouder dan dit aantal minuten tellen niet als actueel") long recencyMinutes,
        Ingest ingest,
        City city,
        List<Wijk> wijken,
        List<Station> stations,
        Network network,
        List<HeatPoint> heatmapPoints) {

    public static final int SCHEMA_VERSION = 1;

    public record Ingest(String lastSuccessAt, String lastAttemptAt, String lastError,
                         int consecutiveFailures, boolean stale) {
    }

    public record City(Float avgTemp, Float avgPm25, int wijkenWithData, WijkRef warmest, WijkRef coldest,
                       Change biggestChange) {
    }

    public record WijkRef(long id, String name, Float avgTemp) {
    }

    @Schema(description = "Verandering van de gemiddelde temperatuur in een wijk over de laatste 24 uur")
    public record Change(long id, String name, Float from, Float to, Float delta) {
    }

    public record Wijk(long id, String name, Float avgTemp, Float avgPm25, int stationCount,
                       String lastMeasurementAt, List<SeriesPoint> series) {
    }

    @Schema(description = "Een uur uit de reeks van 24 uur; t is het einde van dat uur")
    public record SeriesPoint(String t, Float avgTemp, Float avgPm25) {
    }

    public record Latest(String timestamp, Float temperature, Float humidity, Float pm25, Float pm10) {
    }

    public record Flags(boolean tempError, boolean humError, boolean stofError, boolean locError) {
    }

    @Schema(description = "status is ok of no_current_value; reason is dan uitval, sensorfout of late_meting")
    public record Station(long id, String name, Float latitude, Float longitude, Boolean isActive,
                          Latest latest, Flags flags, String status, String reason, String lastMeasurementAt) {
    }

    public record Network(int stationsTotal, int active, int offline, int withSensorErrors,
                          Float coveragePercent24h) {
    }

    public record HeatPoint(long stationId, Float latitude, Float longitude, Float temperature, Float pm25,
                            long ageMinutes) {
    }

    /** Kopie zonder wisselende velden, voor de ETag: dezelfde data geeft dezelfde ETag. */
    public Overview withoutVolatileFields() {
        Ingest stable = ingest == null ? null
                : new Ingest(ingest.lastSuccessAt(), null, ingest.lastError(), ingest.consecutiveFailures(), ingest.stale());
        return new Overview(schemaVersion, null, dataAsOf, recencyMinutes, stable, city, wijken, stations, network,
                heatmapPoints);
    }
}
