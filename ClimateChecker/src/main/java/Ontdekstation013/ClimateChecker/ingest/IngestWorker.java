package Ontdekstation013.ClimateChecker.ingest;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.snapshot.SnapshotService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.random.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * De enige die meetjestad.net aanroept. Haalt periodiek en incrementeel nieuwe metingen op, slaat ze
 * idempotent op in de database en ververst daarna de snapshots. Een run kan nooit overlappen
 * (single-flight lock en een enkele thread) en respecteert een minimale tussentijd en een maximum
 * aantal verzoeken per uur. Na fouten wacht hij exponentieel langer, met jitter.
 */
@Slf4j
@Component
public class IngestWorker {

    public enum Outcome { SUCCESS, FAILURE, RATE_LIMITED, BUSY }

    public record RunResult(Outcome outcome, Duration nextDelay) {
    }

    private static final int MAX_ERROR_LENGTH = 200;

    private final IngestProperties properties;
    private final IngestState state;
    private final MeasurementSource source;
    private final MeasurementIngestService ingestService;
    private final SnapshotService snapshotService;
    private final Clock clock;
    private final DelayedRunner runner;
    private final RequestLimiter limiter;
    private final BackoffPolicy backoff;
    private final ReentrantLock singleFlight = new ReentrantLock();
    private Instant lastFetchedUntil;

    @Autowired
    public IngestWorker(IngestProperties properties, IngestState state, MeasurementSource source,
                        MeasurementIngestService ingestService, SnapshotService snapshotService,
                        Clock clock, DelayedRunner runner) {
        this(properties, state, source, ingestService, snapshotService, clock, runner, new Random());
    }

    IngestWorker(IngestProperties properties, IngestState state, MeasurementSource source,
                 MeasurementIngestService ingestService, SnapshotService snapshotService,
                 Clock clock, DelayedRunner runner, RandomGenerator random) {
        this.properties = properties;
        this.state = state;
        this.source = source;
        this.ingestService = ingestService;
        this.snapshotService = snapshotService;
        this.clock = clock;
        this.runner = runner;
        this.limiter = new RequestLimiter(Duration.ofSeconds(properties.getMinRequestIntervalSeconds()),
                properties.getMaxRequestsPerHour());
        this.backoff = new BackoffPolicy(Duration.ofSeconds(properties.getBackoffInitialSeconds()),
                Duration.ofSeconds(properties.getBackoffMaxSeconds()), random);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!properties.isEnabled()) {
            log.info("Ingest staat uit (ingest.enabled=false): meetjestad.net wordt niet aangeroepen");
            return;
        }
        log.info("Ingest gestart: interval {} s, maximaal {} verzoeken per uur",
                properties.getIntervalSeconds(), properties.getMaxRequestsPerHour());
        runner.schedule(this::loop, Duration.ZERO);
    }

    private void loop() {
        Duration delay = Duration.ofSeconds(properties.getIntervalSeconds());
        try {
            delay = runOnce().nextDelay();
        } catch (RuntimeException e) {
            // Nooit de lus laten sterven.
            log.error("Onverwachte fout in de ingest-lus", e);
        }
        state.recordNextAttempt(clock.instant().plus(delay));
        runner.schedule(this::loop, delay);
    }

    /** Eén ingest-run. Geeft terug hoe het ging en hoe lang gewacht moet worden tot de volgende. */
    public RunResult runOnce() {
        if (!singleFlight.tryLock()) {
            return new RunResult(Outcome.BUSY, Duration.ofSeconds(properties.getMinRequestIntervalSeconds()));
        }
        try {
            Instant now = clock.instant();
            Duration wait = limiter.tryAcquire(now);
            if (!wait.isZero()) {
                log.info("Ingest wacht {} s vanwege de limiet op verzoeken", wait.toSeconds());
                return new RunResult(Outcome.RATE_LIMITED, wait);
            }

            state.recordAttempt(now);
            try {
                Instant start = ingestService.windowStart(now, lastFetchedUntil);
                List<Measurement> fetched = source.fetch(start, now);
                int stored = ingestService.store(fetched);
                lastFetchedUntil = now;
                state.recordSuccess(now, stored);
                log.info("Ingest geslaagd: {} opgehaald, {} nieuw opgeslagen", fetched.size(), stored);
                return new RunResult(Outcome.SUCCESS, Duration.ofSeconds(properties.getIntervalSeconds()));
            } catch (RuntimeException e) {
                state.recordFailure(describe(e));
                int failures = state.get().consecutiveFailures();
                Duration delay = backoff.delayAfterFailure(failures, retryAfter(e));
                log.warn("Ingest mislukt ({} opeenvolgend): {}. Nieuwe poging over {} s",
                        failures, describe(e), delay.toSeconds());
                return new RunResult(Outcome.FAILURE, delay);
            }
        } finally {
            refreshSnapshots();
            singleFlight.unlock();
        }
    }

    private void refreshSnapshots() {
        try {
            snapshotService.rebuild();
        } catch (RuntimeException e) {
            log.error("Snapshots verversen mislukt: {}", describe(e));
        }
    }

    // Alleen klasse en korte melding, geen volledige stacktrace of URL met parameters.
    static String describe(Throwable e) {
        String message = e instanceof HttpStatusCodeException http
                ? "HTTP " + http.getStatusCode().value()
                : String.valueOf(e.getMessage());
        String text = e.getClass().getSimpleName() + ": " + message;
        return text.length() > MAX_ERROR_LENGTH ? text.substring(0, MAX_ERROR_LENGTH) : text;
    }

    private static Duration retryAfter(RuntimeException e) {
        if (e instanceof HttpStatusCodeException http && http.getResponseHeaders() != null) {
            String value = http.getResponseHeaders().getFirst("Retry-After");
            if (value != null) {
                try {
                    return Duration.ofSeconds(Long.parseLong(value.trim()));
                } catch (NumberFormatException ignored) {
                    // een datum-vorm van Retry-After negeren we, de backoff dekt dat af
                }
            }
        }
        return null;
    }
}
