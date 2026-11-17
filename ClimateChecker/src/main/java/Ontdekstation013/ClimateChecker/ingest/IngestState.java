package Ontdekstation013.ClimateChecker.ingest;

import java.time.Instant;
import org.springframework.stereotype.Component;

/** Toestand van de ingest: laatste poging, laatste succes en fouten. Thread-safe. */
@Component
public class IngestState {

    /** Onveranderlijke momentopname voor lezers. */
    public record Status(Instant lastSuccessAt, Instant lastAttemptAt, String lastError,
                         int consecutiveFailures, Instant nextAttemptAt, int lastStoredCount) {
    }

    private volatile Status status = new Status(null, null, null, 0, null, 0);

    public Status get() {
        return status;
    }

    public synchronized void recordAttempt(Instant now) {
        Status s = status;
        status = new Status(s.lastSuccessAt(), now, s.lastError(), s.consecutiveFailures(), s.nextAttemptAt(), s.lastStoredCount());
    }

    public synchronized void recordSuccess(Instant now, int storedCount) {
        Status s = status;
        status = new Status(now, s.lastAttemptAt(), null, 0, s.nextAttemptAt(), storedCount);
    }

    public synchronized void recordFailure(String error) {
        Status s = status;
        status = new Status(s.lastSuccessAt(), s.lastAttemptAt(), error, s.consecutiveFailures() + 1, s.nextAttemptAt(), s.lastStoredCount());
    }

    public synchronized void recordNextAttempt(Instant next) {
        Status s = status;
        status = new Status(s.lastSuccessAt(), s.lastAttemptAt(), s.lastError(), s.consecutiveFailures(), next, s.lastStoredCount());
    }
}
