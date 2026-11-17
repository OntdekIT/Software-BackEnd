package Ontdekstation013.ClimateChecker.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MeasurementIngestServiceTest extends AbstractApiTest {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired
    private MeasurementIngestService service;

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private IngestProperties properties;

    private static Measurement measurement(long stationId, Duration ago, Float temperature) {
        Station station = new Station();
        station.setStationid(stationId);
        Measurement m = new Measurement(NOW.minus(ago), 51.56f, 5.08f, temperature, 60f, 8f, 12f);
        m.setStation(station);
        return m;
    }

    @Test
    void storingTheSameBatchTwiceDoesNotCreateDuplicates() {
        long station = createStation(null).getStationid();
        List<Measurement> batch = List.of(
                measurement(station, Duration.ofMinutes(10), 10f), measurement(station, Duration.ofMinutes(20), 11f));

        assertThat(service.store(batch)).isEqualTo(2);
        // Dezelfde metingen als nieuwe objecten, zoals een overlappend venster ze oplevert.
        assertThat(service.store(List.of(
                measurement(station, Duration.ofMinutes(10), 10f), measurement(station, Duration.ofMinutes(20), 11f))))
                .isZero();

        assertThat(measurementRepository.count()).isEqualTo(2);
    }

    @Test
    void anOverlappingBatchOnlyStoresTheNewMeasurements() {
        long station = createStation(null).getStationid();
        service.store(List.of(measurement(station, Duration.ofMinutes(30), 10f), measurement(station, Duration.ofMinutes(20), 10f)));

        int stored = service.store(List.of(
                measurement(station, Duration.ofMinutes(20), 10f), measurement(station, Duration.ofMinutes(10), 12f)));

        assertThat(stored).isEqualTo(1);
        assertThat(measurementRepository.count()).isEqualTo(3);
    }

    @Test
    void duplicatesInsideOneBatchAreStoredOnce() {
        long station = createStation(null).getStationid();

        int stored = service.store(List.of(
                measurement(station, Duration.ofMinutes(10), 10f), measurement(station, Duration.ofMinutes(10), 10f)));

        assertThat(stored).isEqualTo(1);
    }

    @Test
    void sameTimestampOnDifferentStationsAreDifferentMeasurements() {
        long a = createStation(null).getStationid();
        long b = createStation(null).getStationid();

        int stored = service.store(List.of(measurement(a, Duration.ofMinutes(10), 10f), measurement(b, Duration.ofMinutes(10), 10f)));

        assertThat(stored).isEqualTo(2);
    }

    @Test
    void anUnknownStationIsCreatedSoTheMeasurementIsNotLost() {
        long unknown = 880_001L;

        service.store(List.of(measurement(unknown, Duration.ofMinutes(10), 10f)));

        Station created = stationRepository.getMeetstationByStationid(unknown);
        assertThat(created).isNotNull();
        assertThat(created.getTempError()).isFalse();
        assertThat(measurementRepository.count()).isEqualTo(1);
    }

    @Test
    void anEmptyBatchStoresNothing() {
        assertThat(service.store(List.of())).isZero();
    }

    @Test
    void theWindowAfterAPreviousRunStartsAtItsEndMinusTheOverlap() {
        Instant previousEnd = NOW.minus(Duration.ofMinutes(5));

        assertThat(service.windowStart(NOW, previousEnd))
                .isEqualTo(previousEnd.minus(Duration.ofMinutes(properties.getOverlapMinutes())));
    }

    @Test
    void anEmptyDatabaseStartsWithTheInitialWindow() {
        assertThat(service.windowStart(NOW, null)).isEqualTo(NOW.minus(Duration.ofHours(properties.getInitialWindowHours())));
    }

    @Test
    void aRestartContinuesFromTheNewestStoredMeasurement() {
        long station = createStation(null).getStationid();
        service.store(List.of(measurement(station, Duration.ofMinutes(45), 10f)));

        assertThat(service.windowStart(NOW, null))
                .isEqualTo(NOW.minus(Duration.ofMinutes(45)).minus(Duration.ofMinutes(properties.getOverlapMinutes())));
    }

    @Test
    void aLongOutageIsCappedAtTheMaximumCatchUpPeriod() {
        long station = createStation(null).getStationid();
        service.store(List.of(measurement(station, Duration.ofDays(60), 10f)));

        assertThat(service.windowStart(NOW, null)).isEqualTo(NOW.minus(Duration.ofHours(properties.getMaxCatchUpHours())));
    }
}
