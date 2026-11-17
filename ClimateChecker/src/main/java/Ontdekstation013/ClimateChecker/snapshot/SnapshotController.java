package Ontdekstation013.ClimateChecker.snapshot;

import Ontdekstation013.ClimateChecker.ingest.IngestHealthIndicator;
import Ontdekstation013.ClimateChecker.ingest.IngestProperties;
import Ontdekstation013.ClimateChecker.ingest.IngestState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Snapshot", description = "Vooraf berekende staat van het netwerk en de status van de data-ingest")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SnapshotController {
    private final SnapshotService snapshotService;
    private final SnapshotProperties properties;
    private final IngestProperties ingestProperties;
    private final IngestState ingestState;
    private final IngestHealthIndicator health;
    private final Clock clock;

    @Operation(summary = "Geef de volledige actuele staat van het netwerk in een keer",
            description = "Wijken (actuele gemiddelden en 24-uurs reeks), stad, stations met laatste meting en reden bij "
                    + "ontbrekende data, netwerkgezondheid en heatmap-punten. Alle tijden zijn ISO-8601 UTC. "
                    + "Ondersteunt ETag en If-None-Match (304).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gelukt",
                    content = @Content(schema = @Schema(implementation = Overview.class))),
            @ApiResponse(responseCode = "304", description = "Ongewijzigd sinds de opgegeven ETag")})
    @GetMapping(value = "/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> getSnapshot(@RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        MapSnapshot snapshot = snapshotService.current();
        ResponseEntity.BodyBuilder response = ResponseEntity.status(matches(ifNoneMatch, snapshot.etag())
                        ? HttpStatus.NOT_MODIFIED : HttpStatus.OK)
                .eTag(snapshot.etag())
                .cacheControl(CacheControl.maxAge(properties.getCacheMaxAgeSeconds(), TimeUnit.SECONDS).cachePublic())
                .contentType(MediaType.APPLICATION_JSON);
        return matches(ifNoneMatch, snapshot.etag()) ? response.build() : response.body(snapshot.overviewJson());
    }

    @Operation(summary = "Status van de data-ingest en de snapshots")
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        IngestState.Status status = ingestState.get();
        var snapshot = snapshotService.peek();
        Map<String, Object> ingest = new java.util.LinkedHashMap<>();
        ingest.put("enabled", ingestProperties.isEnabled());
        ingest.put("lastSuccessAt", status.lastSuccessAt());
        ingest.put("lastAttemptAt", status.lastAttemptAt());
        ingest.put("lastError", status.lastError());
        ingest.put("consecutiveFailures", status.consecutiveFailures());
        ingest.put("nextAttemptAt", status.nextAttemptAt());
        ingest.put("lastStoredCount", status.lastStoredCount());
        ingest.put("stale", health.isStale(clock.instant()));
        ingest.put("staleAfterMinutes", Duration.ofMinutes(ingestProperties.getStaleAfterMinutes()).toMinutes());

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("dataAsOf", snapshotService.dataAsOf());
        result.put("ingest", ingest);
        result.put("snapshot", snapshot.map(s -> Map.<String, Object>of(
                "version", s.version(), "builtAt", s.builtAt(),
                "stations", s.overview().stations().size(), "wijken", s.overview().wijken().size(),
                "payloadBytes", s.overviewJson().length)).orElse(null));
        return result;
    }

    private static boolean matches(String ifNoneMatch, String etag) {
        if (ifNoneMatch == null) {
            return false;
        }
        return Arrays.stream(ifNoneMatch.split(",")).map(String::trim).anyMatch(v -> v.equals("*") || v.equals(etag));
    }
}
