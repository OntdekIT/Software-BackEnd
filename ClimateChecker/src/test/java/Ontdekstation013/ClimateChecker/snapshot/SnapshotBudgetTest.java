package Ontdekstation013.ClimateChecker.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementRepository;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoordsRepository;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Tijd- en groottebudget van een snapshot op een realistische omvang: 43 stations met elke 5 minuten
 * een meting over 24 uur, en 70 wijken met een veelhoek van 12 punten. De cijfers worden gelogd.
 */
class SnapshotBudgetTest extends AbstractApiTest {

    @MockBean
    private MeetJeStadService meetJeStadService;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private NeighbourhoodRepository neighbourhoodRepository;

    @Autowired
    private NeighbourhoodCoordsRepository coordsRepository;

    @Test
    void buildTimeAndPayloadStayWithinBudget() throws Exception {
        when(meetJeStadService.getMinuteLimit()).thenReturn(1440);
        when(meetJeStadService.IncorrectValueFilter(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        for (int i = 0; i < 70; i++) {
            Neighbourhood wijk = neighbourhoodRepository.save(new Neighbourhood(8000 + i, "Wijk " + i));
            float lon = 4.95f + (i % 10) * 0.03f;
            float lat = 51.50f + (i / 10) * 0.02f;
            for (int p = 0; p < 12; p++) {
                double angle = 2 * Math.PI * p / 12;
                coordsRepository.save(new NeighbourhoodCoords(8000L * 100 + i * 12 + p,
                        lat + 0.009f * (float) Math.sin(angle), lon + 0.014f * (float) Math.cos(angle), wijk));
            }
        }
        entityManager.flush();
        entityManager.clear();
        Instant now = Instant.now();
        List<Measurement> measurements = new ArrayList<>();
        for (int s = 0; s < 43; s++) {
            Station station = createStation(null);
            float lon = 4.95f + (s % 10) * 0.03f;
            float lat = 51.50f + (s / 10) * 0.02f;
            for (int minute = 0; minute < 24 * 60; minute += 5) {
                Measurement m = new Measurement(now.minus(Duration.ofMinutes(minute)), lat, lon,
                        10f + s % 7 + minute / 600f, 60f, 5f + s % 3, 9f);
                m.setStation(station);
                measurements.add(m);
            }
        }
        measurementRepository.saveAll(measurements);
        measurementRepository.flush();

        snapshotService.rebuild();   // opwarmen (JIT, queries)
        long started = System.nanoTime();
        MapSnapshot snapshot = snapshotService.rebuild();
        long buildMillis = (System.nanoTime() - started) / 1_000_000;

        ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(gzipped)) {
            out.write(snapshot.overviewJson());
        }
        System.out.printf("SNAPSHOT-BUDGET: %d stations, %d wijken, %d metingen: bouwtijd %d ms, payload %d kB, gzip %d kB%n",
                snapshot.overview().stations().size(), snapshot.overview().wijken().size(), measurements.size(),
                buildMillis, snapshot.overviewJson().length / 1024, gzipped.size() / 1024);

        assertThat(snapshot.overview().stations()).hasSize(43);
        assertThat(snapshot.overview().wijken()).hasSize(70);
        assertThat(buildMillis).as("bouwtijd in ms").isLessThan(5_000);
        assertThat(snapshot.overviewJson().length).as("payload in bytes").isLessThan(400 * 1024);
    }
}
