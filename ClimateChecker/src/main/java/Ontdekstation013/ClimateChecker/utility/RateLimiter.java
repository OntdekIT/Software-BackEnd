package Ontdekstation013.ClimateChecker.utility;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory fixed-window rate limiter.
 * <p>
 * Guards the authentication endpoints against brute-forcing the short numeric
 * codes and against mail-spamming. It is intentionally dependency-free and
 * per-instance; for a multi-instance deployment this should be backed by a
 * shared store (e.g. Redis) instead.
 */
@Component
public class RateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Registers an attempt for the given key and reports whether it is allowed.
     *
     * @param key a caller-scoped identifier (e.g. "login:1.2.3.4:user@example.com")
     * @return true if the attempt is within the limit, false if it should be rejected
     */
    public boolean isAllowed(String key) {
        Instant now = Instant.now();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired(now)) {
                return new Window(now);
            }
            existing.increment();
            return existing;
        });
        return window.count() <= MAX_ATTEMPTS;
    }

    private static final class Window {
        private final Instant start;
        private int count;

        private Window(Instant start) {
            this.start = start;
            this.count = 1;
        }

        private boolean isExpired(Instant now) {
            return start.plus(WINDOW).isBefore(now);
        }

        private void increment() {
            count++;
        }

        private int count() {
            return count;
        }
    }
}
