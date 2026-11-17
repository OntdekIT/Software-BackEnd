package Ontdekstation013.ClimateChecker.ingest;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Eén achtergrondthread: taken kunnen daardoor nooit overlappen. */
public class ExecutorDelayedRunner implements DelayedRunner {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "measurement-ingest");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void schedule(Runnable task, Duration delay) {
        executor.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        executor.shutdownNow();
    }
}
