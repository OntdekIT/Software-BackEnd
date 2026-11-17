package Ontdekstation013.ClimateChecker.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.ingest.IngestWorker.Outcome;
import Ontdekstation013.ClimateChecker.ingest.IngestWorker.RunResult;
import Ontdekstation013.ClimateChecker.snapshot.SnapshotService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

/** Gedrag van de worker met een nepklok, een nepbron en een nepplanner. */
class IngestWorkerTest {

    private static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    private IngestProperties properties;
    private IngestState state;
    private MutableClock clock;
    private MeasurementSource source;
    private MeasurementIngestService ingestService;
    private SnapshotService snapshotService;
    private final List<Duration> scheduledDelays = new ArrayList<>();
    private final List<Runnable> scheduledTasks = new ArrayList<>();
    private IngestWorker worker;

    private static final RandomGenerator NO_JITTER = new RandomGenerator() {
        @Override
        public long nextLong() {
            return 0;
        }

        @Override
        public double nextDouble() {
            return 1.0;
        }
    };

    @BeforeEach
    void setUp() {
        properties = new IngestProperties();
        properties.setIntervalSeconds(300);
        properties.setMinRequestIntervalSeconds(60);
        properties.setMaxRequestsPerHour(30);
        properties.setBackoffInitialSeconds(30);
        properties.setBackoffMaxSeconds(1800);
        properties.setOverlapMinutes(10);
        state = new IngestState();
        clock = new MutableClock(T0);
        source = mock(MeasurementSource.class);
        ingestService = mock(MeasurementIngestService.class);
        snapshotService = mock(SnapshotService.class);
        when(ingestService.windowStart(any(), any())).thenReturn(T0.minusSeconds(3600));
        when(ingestService.store(any())).thenReturn(7);
        worker = new IngestWorker(properties, state, source, ingestService, snapshotService, clock,
                new DelayedRunner() {
                    @Override
                    public void schedule(Runnable task, Duration delay) {
                        scheduledTasks.add(task);
                        scheduledDelays.add(delay);
                    }

                    @Override
                    public void shutdown() {
                    }
                }, NO_JITTER);
    }

    @Test
    void aSuccessfulRunStoresRecordsStateAndRefreshesTheSnapshots() {
        when(source.fetch(any(), any())).thenReturn(List.of(new Measurement()));

        RunResult result = worker.runOnce();

        assertThat(result.outcome()).isEqualTo(Outcome.SUCCESS);
        assertThat(result.nextDelay()).isEqualTo(Duration.ofSeconds(300));
        IngestState.Status status = state.get();
        assertThat(status.lastSuccessAt()).isEqualTo(T0);
        assertThat(status.lastAttemptAt()).isEqualTo(T0);
        assertThat(status.consecutiveFailures()).isZero();
        assertThat(status.lastError()).isNull();
        assertThat(status.lastStoredCount()).isEqualTo(7);
        verify(snapshotService).rebuild();
    }

    @Test
    void theWindowIsIncrementalAndStartsWhereTheLastSuccessfulFetchEnded() {
        when(source.fetch(any(), any())).thenReturn(List.of());

        worker.runOnce();
        clock.advance(Duration.ofMinutes(5));
        worker.runOnce();

        // De eerste run kent geen vorig einde, de tweede wel: het einde van de eerste.
        verify(ingestService).windowStart(T0, null);
        verify(ingestService).windowStart(T0.plus(Duration.ofMinutes(5)), T0);
    }

    @Test
    void aFailedRunDoesNotMoveTheWindow() {
        when(source.fetch(any(), any())).thenReturn(List.of())
                .thenThrow(new ResourceAccessException("time-out"))
                .thenReturn(List.of());

        worker.runOnce();
        clock.advance(Duration.ofMinutes(5));
        worker.runOnce();
        clock.advance(Duration.ofMinutes(5));
        worker.runOnce();

        verify(ingestService, times(2)).windowStart(any(), org.mockito.ArgumentMatchers.eq(T0));
    }

    @Test
    void failuresBackOffExponentiallyAndASuccessResetsIt() {
        when(source.fetch(any(), any()))
                .thenThrow(new ResourceAccessException("time-out"))
                .thenThrow(new ResourceAccessException("time-out"))
                .thenThrow(new ResourceAccessException("time-out"))
                .thenReturn(List.of())
                .thenThrow(new ResourceAccessException("time-out"));

        List<Duration> delays = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            delays.add(worker.runOnce().nextDelay());
            clock.advance(Duration.ofHours(1));
        }

        assertThat(delays).containsExactly(Duration.ofSeconds(30), Duration.ofSeconds(60), Duration.ofSeconds(120),
                Duration.ofSeconds(300), Duration.ofSeconds(30));
        assertThat(state.get().consecutiveFailures()).isEqualTo(1);
    }

    @Test
    void errorsAreRecordedWithoutSecretsOrLongTraces() {
        when(source.fetch(any(), any())).thenThrow(new ResourceAccessException("x".repeat(1000)));

        worker.runOnce();

        assertThat(state.get().lastError()).startsWith("ResourceAccessException").hasSizeLessThanOrEqualTo(200);
        assertThat(state.get().lastSuccessAt()).isNull();
    }

    @Test
    void aRetryAfterHeaderOf429IsHonoured() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", "900");
        when(source.fetch(any(), any())).thenThrow(HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", headers, new byte[0], null));

        RunResult result = worker.runOnce();

        assertThat(result.outcome()).isEqualTo(Outcome.FAILURE);
        assertThat(result.nextDelay()).isEqualTo(Duration.ofSeconds(900));
        assertThat(state.get().lastError()).isEqualTo("TooManyRequests: HTTP 429");
    }

    @Test
    void theMinimumIntervalBetweenRequestsIsHonouredWithoutCallingTheSource() {
        when(source.fetch(any(), any())).thenReturn(List.of());

        worker.runOnce();
        clock.advance(Duration.ofSeconds(10));
        RunResult second = worker.runOnce();

        assertThat(second.outcome()).isEqualTo(Outcome.RATE_LIMITED);
        assertThat(second.nextDelay()).isEqualTo(Duration.ofSeconds(50));
        verify(source, times(1)).fetch(any(), any());
        // Een geweigerde run telt niet als fout.
        assertThat(state.get().consecutiveFailures()).isZero();
    }

    @Test
    void theHourlyMaximumIsHonoured() {
        properties.setMaxRequestsPerHour(2);
        properties.setMinRequestIntervalSeconds(1);
        worker = new IngestWorker(properties, state, source, ingestService, snapshotService, clock,
                mock(DelayedRunner.class), NO_JITTER);
        when(source.fetch(any(), any())).thenReturn(List.of());

        worker.runOnce();
        clock.advance(Duration.ofSeconds(5));
        worker.runOnce();
        clock.advance(Duration.ofSeconds(5));
        RunResult third = worker.runOnce();

        assertThat(third.outcome()).isEqualTo(Outcome.RATE_LIMITED);
        verify(source, times(2)).fetch(any(), any());
    }

    @Test
    void runsNeverOverlap() throws Exception {
        CountDownLatch inFetch = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger fetches = new AtomicInteger();
        when(source.fetch(any(), any())).thenAnswer(invocation -> {
            fetches.incrementAndGet();
            inFetch.countDown();
            release.await(5, TimeUnit.SECONDS);
            return List.of();
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<RunResult> first = executor.submit(worker::runOnce);
            assertThat(inFetch.await(5, TimeUnit.SECONDS)).isTrue();

            RunResult second = worker.runOnce();

            assertThat(second.outcome()).isEqualTo(Outcome.BUSY);
            release.countDown();
            assertThat(first.get(5, TimeUnit.SECONDS).outcome()).isEqualTo(Outcome.SUCCESS);
            assertThat(fetches.get()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void theSnapshotsAreAlsoRefreshedAfterAFailedRunSoStatusStaysCurrent() {
        when(source.fetch(any(), any())).thenThrow(new ResourceAccessException("time-out"));

        worker.runOnce();

        verify(snapshotService).rebuild();
    }

    @Test
    void aFailingSnapshotRebuildDoesNotBreakTheRun() {
        when(source.fetch(any(), any())).thenReturn(List.of());
        when(snapshotService.rebuild()).thenThrow(new IllegalStateException("kapot"));

        assertThat(worker.runOnce().outcome()).isEqualTo(Outcome.SUCCESS);
    }

    @Test
    void theSchedulingLoopFollowsIntervalAndBackoffAndKeepsGoing() {
        when(source.fetch(any(), any()))
                .thenReturn(List.of())
                .thenThrow(new ResourceAccessException("time-out"))
                .thenThrow(new ResourceAccessException("time-out"))
                .thenReturn(List.of());

        worker.start();
        assertThat(scheduledDelays).containsExactly(Duration.ZERO);
        for (int i = 0; i < 4; i++) {
            clock.advance(Duration.ofHours(1));
            scheduledTasks.get(scheduledTasks.size() - 1).run();
        }

        assertThat(scheduledDelays).containsExactly(Duration.ZERO, Duration.ofSeconds(300), Duration.ofSeconds(30),
                Duration.ofSeconds(60), Duration.ofSeconds(300));
        assertThat(state.get().nextAttemptAt()).isEqualTo(clock.instant().plusSeconds(300));
    }

    @Test
    void theKillSwitchPreventsAnyScheduling() {
        properties.setEnabled(false);

        worker.start();

        assertThat(scheduledTasks).isEmpty();
        verify(source, never()).fetch(any(), any());
    }
}
