package Ontdekstation013.ClimateChecker.snapshot;

import Ontdekstation013.ClimateChecker.features.measurement.LocalMeasurementReader;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementRepository;
import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodRepository;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodTimelapseResponse;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.ingest.IngestHealthIndicator;
import Ontdekstation013.ClimateChecker.ingest.IngestState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Bouwt en bewaart de snapshots. {@link #rebuild()} rekent alles buiten de lock uit en wisselt het
 * resultaat dan in een keer (AtomicReference): lezers zien altijd de oude of de nieuwe staat, nooit
 * een mengsel. Zonder snapshot (net gestart) wordt er synchronisch uit de database een gebouwd, dus
 * er is nooit een live-aanroep naar meetjestad.net nodig.
 */
@Slf4j
@Service
public class SnapshotService {
    /** Aantal stappen waarvoor een time-lapse vooraf berekend wordt (de standaard 24, en 25 voor elk uur inclusief begin en eind). */
    static final List<Integer> TIMELAPSE_STEPS = List.of(24, 25);

    private final StationService stationService;
    private final NeighbourhoodService neighbourhoodService;
    private final NeighbourhoodRepository neighbourhoodRepository;
    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final LocalMeasurementReader reader;
    private final IngestState ingestState;
    private final IngestHealthIndicator health;
    private final SnapshotProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final TransactionTemplate readOnlyTransaction;

    private final AtomicReference<MapSnapshot> current = new AtomicReference<>();
    private final AtomicLong versions = new AtomicLong();
    private final ReentrantLock buildLock = new ReentrantLock();

    public SnapshotService(StationService stationService, NeighbourhoodService neighbourhoodService,
                           NeighbourhoodRepository neighbourhoodRepository, StationRepository stationRepository,
                           MeasurementRepository measurementRepository, LocalMeasurementReader reader,
                           IngestState ingestState, IngestHealthIndicator health, SnapshotProperties properties,
                           ObjectMapper objectMapper, Clock clock, PlatformTransactionManager transactionManager) {
        this.stationService = stationService;
        this.neighbourhoodService = neighbourhoodService;
        this.neighbourhoodRepository = neighbourhoodRepository;
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.reader = reader;
        this.ingestState = ingestState;
        this.health = health;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
        // De worker draait buiten een verzoek, dus zonder open sessie: lazy relaties (wijkcoordinaten) vragen een transactie.
        this.readOnlyTransaction = new TransactionTemplate(transactionManager);
        this.readOnlyTransaction.setReadOnly(true);
    }

    /** Warm de snapshot op uit de database zodra de app klaar is, vooraleer verkeer er last van heeft. */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        try {
            current();
        } catch (RuntimeException e) {
            log.error("Snapshot opwarmen mislukt, wordt bij het eerste verzoek opnieuw geprobeerd", e);
        }
    }

    /** De huidige snapshot; bouwt er een uit de database als die er nog niet is. */
    public MapSnapshot current() {
        MapSnapshot snapshot = current.get();
        return snapshot != null ? snapshot : rebuild();
    }

    /** Alleen de al gebouwde snapshot, zonder te bouwen. */
    public Optional<MapSnapshot> peek() {
        return Optional.ofNullable(current.get());
    }

    /** Het moment van de laatste geslaagde ingest, of de nieuwste meting in de snapshot als die er (nog) niet was. */
    public Instant dataAsOf() {
        Instant success = ingestState.get().lastSuccessAt();
        if (success != null) {
            return success;
        }
        return peek().map(MapSnapshot::dataAsOf).orElse(null);
    }

    /** De snapshot als een verzoek voor {@code timestamp} er binnen de marge van "nu" op past. */
    public Optional<MapSnapshot> servableAt(Instant timestamp) {
        Duration tolerance = Duration.ofSeconds(properties.getNowToleranceSeconds());
        if (Duration.between(timestamp, clock.instant()).abs().compareTo(tolerance) > 0) {
            return Optional.empty();
        }
        return Optional.of(current());
    }

    /** De vooraf berekende time-lapse als het verzoek de standaard 24 uur tot "nu" is met een bekend aantal stappen. */
    public Optional<NeighbourhoodTimelapseResponse> timelapseFor(Instant from, Instant to, int steps) {
        Duration tolerance = Duration.ofSeconds(properties.getNowToleranceSeconds());
        boolean endsNow = Duration.between(to, clock.instant()).abs().compareTo(tolerance) <= 0;
        boolean spansDay = Duration.between(from, to).minus(Duration.ofHours(24)).abs().compareTo(Duration.ofMinutes(1)) <= 0;
        if (!endsNow || !spansDay || !TIMELAPSE_STEPS.contains(steps)) {
            return Optional.empty();
        }
        return Optional.ofNullable(current().timelapses().get(steps));
    }

    /** Bouwt alle snapshots opnieuw en wisselt ze atomair in. Geeft de nieuwe snapshot terug. */
    public MapSnapshot rebuild() {
        buildLock.lock();
        try {
            long started = System.nanoTime();
            MapSnapshot snapshot = readOnlyTransaction.execute(status -> build());
            current.set(snapshot);
            log.info("Snapshot v{} gebouwd in {} ms: {} stations, {} wijken, payload {} kB",
                    snapshot.version(), (System.nanoTime() - started) / 1_000_000,
                    snapshot.overview().stations().size(), snapshot.overview().wijken().size(),
                    snapshot.overviewJson().length / 1024);
            return snapshot;
        } finally {
            buildLock.unlock();
        }
    }

    private MapSnapshot build() {
        Instant now = clock.instant().truncatedTo(ChronoUnit.MINUTES);
        Duration recency = Duration.ofMinutes(properties.getRecencyMinutes());

        var stations = stationService.getStationsWithMeasurementsUncached(now);
        var neighbourhoodDtos = neighbourhoodService.computeNeighbourhoodsAtTime(now);
        Map<Integer, NeighbourhoodTimelapseResponse> timelapses = new HashMap<>();
        for (int steps : TIMELAPSE_STEPS) {
            timelapses.put(steps, neighbourhoodService.computeNeighbourhoodTimelapse(now.minus(Duration.ofHours(24)), now, steps));
        }

        List<Measurement> measurements = reader.getMeasurements(now.minus(Duration.ofHours(24)), now, false);
        Map<Long, Instant> lastTimestamps = new HashMap<>();
        measurementRepository.findLastTimestampPerStation().forEach(k -> lastTimestamps.put(k.getStationId(), k.getTimestamp()));

        Instant dataAsOf = ingestState.get().lastSuccessAt();
        if (dataAsOf == null) {
            dataAsOf = lastTimestamps.values().stream().max(Instant::compareTo).orElse(null);
        }

        IngestState.Status status = ingestState.get();
        Overview.Ingest ingest = new Overview.Ingest(iso(status.lastSuccessAt()), iso(status.lastAttemptAt()),
                status.lastError(), status.consecutiveFailures(), health.isStale(now));

        Overview overview = OverviewCalculator.calculate(now, recency, wijkInputs(), stationInputs(), measurements,
                lastTimestamps, ingest, dataAsOf);

        try {
            byte[] json = objectMapper.writeValueAsBytes(overview);
            String etag = "W/\"" + sha256(objectMapper.writeValueAsBytes(overview.withoutVolatileFields())) + "\"";
            return new MapSnapshot(versions.incrementAndGet(), now, dataAsOf, stations, neighbourhoodDtos,
                    Map.copyOf(timelapses), overview, json, etag);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Snapshot serialiseren mislukt", e);
        }
    }

    private List<OverviewCalculator.WijkInput> wijkInputs() {
        return neighbourhoodRepository.findAll().stream()
                .map(n -> new OverviewCalculator.WijkInput(n.getId(), n.getName(), polygon(n)))
                .toList();
    }

    private static float[][] polygon(Neighbourhood neighbourhood) {
        return neighbourhood.getCoordinates().stream()
                .map((NeighbourhoodCoords c) -> new float[] {c.getLatitude(), c.getLongitude()})
                .toArray(float[][]::new);
    }

    private List<OverviewCalculator.StationInput> stationInputs() {
        return stationRepository.findAll().stream().map(SnapshotService::toInput).toList();
    }

    private static OverviewCalculator.StationInput toInput(Station s) {
        return new OverviewCalculator.StationInput(s.getStationid(), s.getName(), s.getIsActive(),
                s.getLastKnownLatitude(), s.getLastKnownLongitude(), Boolean.TRUE.equals(s.getTempError()),
                Boolean.TRUE.equals(s.getHumError()), Boolean.TRUE.equals(s.getStofError()),
                Boolean.TRUE.equals(s.getLocError()));
    }

    private static String iso(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
