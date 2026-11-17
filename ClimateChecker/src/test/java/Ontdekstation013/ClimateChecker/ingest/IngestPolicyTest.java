package Ontdekstation013.ClimateChecker.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class IngestPolicyTest {

    private static final RandomGenerator NO_JITTER = new FixedRandom(1.0);
    private static final RandomGenerator MAX_JITTER = new FixedRandom(0.0);

    /** Geeft altijd dezelfde waarde, zodat de jitter voorspelbaar is. */
    private record FixedRandom(double value) implements RandomGenerator {
        @Override
        public long nextLong() {
            return 0;
        }

        @Override
        public double nextDouble() {
            return value;
        }
    }

    private static BackoffPolicy backoff(RandomGenerator random) {
        return new BackoffPolicy(Duration.ofSeconds(30), Duration.ofMinutes(30), random);
    }

    @Test
    void backoffDoublesPerConsecutiveFailure() {
        BackoffPolicy policy = backoff(NO_JITTER);

        assertThat(policy.delayAfterFailure(1, null)).isEqualTo(Duration.ofSeconds(30));
        assertThat(policy.delayAfterFailure(2, null)).isEqualTo(Duration.ofSeconds(60));
        assertThat(policy.delayAfterFailure(3, null)).isEqualTo(Duration.ofSeconds(120));
        assertThat(policy.delayAfterFailure(4, null)).isEqualTo(Duration.ofSeconds(240));
    }

    @Test
    void backoffIsCappedAtTheMaximumAndDoesNotOverflow() {
        BackoffPolicy policy = backoff(NO_JITTER);

        assertThat(policy.delayAfterFailure(10, null)).isEqualTo(Duration.ofMinutes(30));
        assertThat(policy.delayAfterFailure(500, null)).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void jitterShortensTheDelayToBetweenHalfAndFull() {
        assertThat(backoff(MAX_JITTER).delayAfterFailure(3, null)).isEqualTo(Duration.ofSeconds(60));
        assertThat(backoff(NO_JITTER).delayAfterFailure(3, null)).isEqualTo(Duration.ofSeconds(120));
    }

    @Test
    void aLongerRetryAfterFromTheSourceWins() {
        BackoffPolicy policy = backoff(NO_JITTER);

        assertThat(policy.delayAfterFailure(1, Duration.ofMinutes(10))).isEqualTo(Duration.ofMinutes(10));
        assertThat(policy.delayAfterFailure(3, Duration.ofSeconds(5))).isEqualTo(Duration.ofSeconds(120));
    }

    @Test
    void limiterEnforcesTheMinimumIntervalBetweenRequests() {
        RequestLimiter limiter = new RequestLimiter(Duration.ofSeconds(60), 100);
        Instant t0 = Instant.parse("2026-03-01T12:00:00Z");

        assertThat(limiter.tryAcquire(t0)).isEqualTo(Duration.ZERO);
        assertThat(limiter.tryAcquire(t0.plusSeconds(20))).isEqualTo(Duration.ofSeconds(40));
        assertThat(limiter.tryAcquire(t0.plusSeconds(60))).isEqualTo(Duration.ZERO);
    }

    @Test
    void limiterEnforcesTheMaximumPerHourAndRecoversAfterwards() {
        RequestLimiter limiter = new RequestLimiter(Duration.ofSeconds(1), 3);
        Instant t0 = Instant.parse("2026-03-01T12:00:00Z");

        assertThat(limiter.tryAcquire(t0)).isEqualTo(Duration.ZERO);
        assertThat(limiter.tryAcquire(t0.plusSeconds(10))).isEqualTo(Duration.ZERO);
        assertThat(limiter.tryAcquire(t0.plusSeconds(20))).isEqualTo(Duration.ZERO);
        // Het vierde verzoek moet wachten tot het eerste uit het uur-venster valt.
        assertThat(limiter.tryAcquire(t0.plusSeconds(30))).isEqualTo(Duration.ofSeconds(3570));
        assertThat(limiter.tryAcquire(t0.plus(Duration.ofHours(1)).plusSeconds(1))).isEqualTo(Duration.ZERO);
    }

    @Test
    void aRefusedRequestIsNotCounted() {
        RequestLimiter limiter = new RequestLimiter(Duration.ofSeconds(60), 2);
        Instant t0 = Instant.parse("2026-03-01T12:00:00Z");
        limiter.tryAcquire(t0);
        for (int i = 1; i < 10; i++) {
            limiter.tryAcquire(t0.plusSeconds(i));
        }

        assertThat(limiter.tryAcquire(t0.plusSeconds(60))).isEqualTo(Duration.ZERO);
    }
}
