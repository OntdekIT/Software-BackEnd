package Ontdekstation013.ClimateChecker.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void allowsAttemptsUpToTheLimit() {
        RateLimiter rateLimiter = new RateLimiter();
        String key = "login:1.2.3.4:user@example.com";

        // The first five attempts within the window are allowed.
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(key), "attempt " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void rejectsAttemptsBeyondTheLimit() {
        RateLimiter rateLimiter = new RateLimiter();
        String key = "login:1.2.3.4:user@example.com";

        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(key);
        }

        // The sixth attempt within the same window is rejected.
        assertFalse(rateLimiter.isAllowed(key));
    }

    @Test
    void tracksDifferentKeysIndependently() {
        RateLimiter rateLimiter = new RateLimiter();
        String attacker = "login:1.2.3.4:victim@example.com";
        String otherUser = "login:5.6.7.8:someone@example.com";

        for (int i = 0; i < 6; i++) {
            rateLimiter.isAllowed(attacker);
        }

        // A different caller/key is not affected by another key hitting the limit.
        assertTrue(rateLimiter.isAllowed(otherUser));
    }
}
