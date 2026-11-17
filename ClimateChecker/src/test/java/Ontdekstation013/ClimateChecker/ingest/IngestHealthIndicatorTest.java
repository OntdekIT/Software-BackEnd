package Ontdekstation013.ClimateChecker.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

class IngestHealthIndicatorTest {

    private IngestProperties properties;
    private IngestState state;
    private MutableClock clock;
    private IngestHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        properties = new IngestProperties();
        properties.setStaleAfterMinutes(30);
        state = new IngestState();
        clock = new MutableClock(Instant.now());
        indicator = new IngestHealthIndicator(properties, state, clock);
    }

    @Test
    void freshDataIsUp() {
        state.recordSuccess(clock.instant().minus(Duration.ofMinutes(5)), 3);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void dataOlderThanTheThresholdIsDegradedNotDown() {
        state.recordSuccess(clock.instant().minus(Duration.ofMinutes(31)), 3);

        assertThat(indicator.health().getStatus()).isEqualTo(IngestHealthIndicator.DEGRADED);
        assertThat(indicator.health().getStatus()).isNotEqualTo(Status.DOWN);
    }

    @Test
    void exactlyAtTheThresholdIsStillFresh() {
        state.recordSuccess(clock.instant().minus(Duration.ofMinutes(30)), 3);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void theThresholdIsConfigurable() {
        properties.setStaleAfterMinutes(5);
        state.recordSuccess(clock.instant().minus(Duration.ofMinutes(6)), 3);

        assertThat(indicator.health().getStatus()).isEqualTo(IngestHealthIndicator.DEGRADED);
    }

    @Test
    void neverSucceededBecomesDegradedOnlyAfterTheThresholdSinceStart() {
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);

        clock.advance(Duration.ofMinutes(31));

        assertThat(indicator.health().getStatus()).isEqualTo(IngestHealthIndicator.DEGRADED);
    }

    @Test
    void aDisabledIngestIsNeverDegraded() {
        properties.setEnabled(false);
        clock.advance(Duration.ofDays(2));

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }
}
