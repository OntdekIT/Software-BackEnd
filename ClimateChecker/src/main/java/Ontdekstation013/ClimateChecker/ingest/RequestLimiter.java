package Ontdekstation013.ClimateChecker.ingest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Houdt het gebruik van de externe bron binnen een minimale tussentijd en een maximum per uur.
 * Niet thread-safe op zichzelf: de worker roept hem alleen aan onder zijn single-flight lock.
 */
public class RequestLimiter {
    private final Duration minInterval;
    private final int maxPerHour;
    private final Deque<Instant> requests = new ArrayDeque<>();

    public RequestLimiter(Duration minInterval, int maxPerHour) {
        this.minInterval = minInterval;
        this.maxPerHour = maxPerHour;
    }

    /** Geeft toestemming voor een verzoek op {@code now} en legt het vast, of geeft de wachttijd terug. */
    public Duration tryAcquire(Instant now) {
        Instant hourAgo = now.minus(Duration.ofHours(1));
        while (!requests.isEmpty() && !requests.peekFirst().isAfter(hourAgo)) {
            requests.pollFirst();
        }
        if (!requests.isEmpty()) {
            Duration sinceLast = Duration.between(requests.peekLast(), now);
            if (sinceLast.compareTo(minInterval) < 0) {
                return minInterval.minus(sinceLast);
            }
        }
        if (requests.size() >= maxPerHour) {
            return Duration.between(now, requests.peekFirst().plus(Duration.ofHours(1)));
        }
        requests.addLast(now);
        return Duration.ZERO;
    }
}
