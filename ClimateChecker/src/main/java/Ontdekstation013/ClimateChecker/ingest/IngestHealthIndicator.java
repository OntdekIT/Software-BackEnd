package Ontdekstation013.ClimateChecker.ingest;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

/**
 * Readiness: DEGRADED (geen DOWN) als de laatste geslaagde ingest te oud is. De app blijft dan
 * verkeer krijgen en toont de laatst bekende data; alleen de monitoring ziet het.
 */
@Component("ingest")
@RequiredArgsConstructor
public class IngestHealthIndicator implements HealthIndicator {
    public static final Status DEGRADED = new Status("DEGRADED");

    private final IngestProperties properties;
    private final IngestState state;
    private final Clock clock;
    private final Instant startedAt = Instant.now();

    @Override
    public Health health() {
        if (!properties.isEnabled()) {
            return Health.up().build();
        }
        return isStale(clock.instant()) ? Health.status(DEGRADED).build() : Health.up().build();
    }

    /** Verouderd: de laatste geslaagde run (of, als die er nog niet was, de start) is te lang geleden. */
    public boolean isStale(Instant now) {
        Instant reference = state.get().lastSuccessAt() != null ? state.get().lastSuccessAt() : startedAt;
        return Duration.between(reference, now).compareTo(Duration.ofMinutes(properties.getStaleAfterMinutes())) > 0;
    }
}
