package Ontdekstation013.ClimateChecker.ingest;

import java.time.Duration;
import java.util.random.RandomGenerator;

/** Exponentiele backoff met jitter na fouten (429, 5xx, time-outs). */
public class BackoffPolicy {
    private final Duration initial;
    private final Duration max;
    private final RandomGenerator random;

    public BackoffPolicy(Duration initial, Duration max, RandomGenerator random) {
        this.initial = initial;
        this.max = max;
        this.random = random;
    }

    /**
     * Wachttijd na de {@code failures}-de opeenvolgende fout: initial * 2^(failures-1), begrensd op
     * max, met jitter tussen 50% en 100% zodat meerdere instanties niet tegelijk terugkomen. Een
     * Retry-After van de bron wint als die langer is.
     */
    public Duration delayAfterFailure(int failures, Duration retryAfter) {
        int exponent = Math.min(Math.max(failures - 1, 0), 20);
        long base = Math.min(max.toMillis(), initial.toMillis() << exponent);
        long jittered = (long) (base * (0.5 + 0.5 * random.nextDouble()));
        Duration delay = Duration.ofMillis(jittered);
        if (retryAfter != null && retryAfter.compareTo(delay) > 0) {
            return retryAfter;
        }
        return delay;
    }
}
