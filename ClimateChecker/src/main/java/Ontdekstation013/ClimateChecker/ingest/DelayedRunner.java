package Ontdekstation013.ClimateChecker.ingest;

import java.time.Duration;

/** Voert een taak eenmalig uit na een vertraging. Een interface zodat tests de tijd kunnen sturen. */
public interface DelayedRunner {
    void schedule(Runnable task, Duration delay);

    void shutdown();
}
