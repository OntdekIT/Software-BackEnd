package Ontdekstation013.ClimateChecker.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import Ontdekstation013.ClimateChecker.features.measurement.LocalMeasurementReader;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Tests voor de cache van de kaartendpoints, tegen de echte Spring-cache (Caffeine) met een
 * nepklok zodat verlopen zonder wachten te testen is.
 */
@SpringBootTest(properties = {
        "spring.config.import=file:src/main/resources/application.properties",
        "spring.profiles.active=test",
        "rate.limit.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:cachetestdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "map.cache.ttl-seconds=60",
        "map.cache.max-entries=3",
        "application.root-user.first-name=Admin",
        "application.root-user.last-name=User",
        "application.root-user.email=admin@example.com",
        "application.root-user.password=password"
})
@Import(MapCacheTest.FakeClockConfig.class)
class MapCacheTest {

    /** Klok die alleen vooruit gaat als de test dat vraagt. */
    static final class FakeTicker implements Ticker {
        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }
    }

    @TestConfiguration
    static class FakeClockConfig {
        @Bean
        @Primary
        FakeTicker fakeTicker() {
            return new FakeTicker();
        }
    }

    @Autowired
    private MeasurementService measurementService;

    @Autowired
    private NeighbourhoodService neighbourhoodService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private FakeTicker ticker;

    @SpyBean
    private LocalMeasurementReader reader;

    @MockBean
    private EmailSenderService emailSenderService;

    private static final Instant MINUTE = Instant.parse("2026-03-01T12:30:00Z");

    @BeforeEach
    void startWithEmptyCaches() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        clearInvocations(reader);
    }

    private void verifyReads(int times) {
        verify(reader, times(times)).getMeasurements(any(), any(), anyBoolean());
    }

    @Test
    void secondCallInTheSameMinuteIsServedFromTheCache() {
        List<MeasurementDto> first = measurementService.getMeasurementsAtTime(MINUTE.plusMillis(123));
        List<MeasurementDto> second = measurementService.getMeasurementsAtTime(MINUTE.plusSeconds(59).plusMillis(999));

        verifyReads(1);
        assertThat(second).isSameAs(first);
    }

    @Test
    void aDifferentMinuteIsComputedAgain() {
        measurementService.getMeasurementsAtTime(MINUTE);
        measurementService.getMeasurementsAtTime(MINUTE.plusSeconds(60));

        verifyReads(2);
    }

    @Test
    void theCacheKeyAndTheComputationUseTheMinuteRoundedDown() {
        measurementService.getMeasurementsAtTime(MINUTE.plusSeconds(42));

        verify(reader).getMeasurements(MINUTE.minus(Duration.ofMinutes(1440)), MINUTE, false);
    }

    @Test
    void anEntryExpiresAfterTheTtl() {
        measurementService.getMeasurementsAtTime(MINUTE);

        ticker.advance(Duration.ofSeconds(59));
        measurementService.getMeasurementsAtTime(MINUTE);
        verifyReads(1);

        ticker.advance(Duration.ofSeconds(2));
        measurementService.getMeasurementsAtTime(MINUTE);
        verifyReads(2);
    }

    @Test
    void theNeighbourhoodListIsCachedPerMinuteToo() {
        neighbourhoodService.getNeighbourhoodsAtTime(MINUTE.plusMillis(5));
        neighbourhoodService.getNeighbourhoodsAtTime(MINUTE.plusSeconds(30));
        verifyReads(1);

        neighbourhoodService.getNeighbourhoodsAtTime(MINUTE.plusSeconds(61));
        verifyReads(2);
    }

    @Test
    void theTimelapseIsCachedOnRoundedFromToAndSteps() {
        Instant to = MINUTE.plusMillis(700);
        Instant from = to.minus(Duration.ofHours(24));

        neighbourhoodService.getNeighbourhoodTimelapse(from, to, 24);
        neighbourhoodService.getNeighbourhoodTimelapse(from.plusSeconds(10), to.plusSeconds(10), 24);
        verifyReads(1);

        // Andere stappen is een andere time-lapse.
        neighbourhoodService.getNeighbourhoodTimelapse(from, to, 12);
        verifyReads(2);
    }

    @Test
    void anInvalidRequestIsNotCached() {
        Instant to = MINUTE;
        for (int i = 0; i < 2; i++) {
            try {
                neighbourhoodService.getNeighbourhoodTimelapse(to, to, 24);
            } catch (RuntimeException expected) {
                // 'from' moet voor 'to' liggen
            }
        }

        verifyReads(0);
    }

    @Test
    void theCacheIsBoundedInSize() {
        // max-entries is 3 in deze test; vijf verschillende minuten blijven niet allemaal staan.
        for (int minute = 0; minute < 5; minute++) {
            measurementService.getMeasurementsAtTime(MINUTE.plusSeconds(60L * minute));
        }
        var nativeCache = ((org.springframework.cache.caffeine.CaffeineCache)
                cacheManager.getCache(CacheNames.STATIONS_WITH_MEASUREMENTS)).getNativeCache();
        nativeCache.cleanUp();

        assertThat(nativeCache.estimatedSize()).isLessThanOrEqualTo(3);
    }
}
