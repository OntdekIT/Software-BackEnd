package Ontdekstation013.ClimateChecker.measurement;

import static org.assertj.core.api.Assertions.assertThat;

import Ontdekstation013.ClimateChecker.features.measurement.LocalMeasurementReader;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementRepository;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import Ontdekstation013.ClimateChecker.utility.MeasurementLogic;
import com.sun.net.httpserver.HttpServer;
import jakarta.persistence.EntityManager;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/**
 * Vergelijkt de lokale route (database) met de oude live-route (meetjestad.net, hier een lokale
 * nep-server met dezelfde gegevens), en controleert dat het aantal queries niet meegroeit met het
 * aantal metingen of stations.
 */
@TestPropertySource(properties = "spring.jpa.properties.hibernate.session_factory.statement_inspector="
        + "Ontdekstation013.ClimateChecker.measurement.LocalMeasurementReaderTest$StatementCounter")
class LocalMeasurementReaderTest extends AbstractApiTest {

    /**
     * Telt de SQL-statements van de huidige thread. Hibernate-statistieken zijn globaal en dus
     * vervuild door achtergrondthreads (zoals de stationmonitor), een teller per thread niet.
     */
    public static class StatementCounter implements StatementInspector {
        static final ThreadLocal<List<String>> STATEMENTS = ThreadLocal.withInitial(ArrayList::new);

        @Override
        public String inspect(String sql) {
            STATEMENTS.get().add(sql);
            return sql;
        }
    }

    private static final Instant NOW = Instant.parse("2026-03-01T12:30:00Z");

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private LocalMeasurementReader reader;

    @Autowired
    private EntityManager entityManager;

    private HttpServer fakeMeetJeStad;
    private String fixtureJson = "[]";

    @BeforeEach
    void startFakeMeetJeStad() throws Exception {
        fakeMeetJeStad = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fakeMeetJeStad.createContext("/data/", exchange -> {
            byte[] body = fixtureJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        fakeMeetJeStad.start();
    }

    @AfterEach
    void stopFakeMeetJeStad() {
        fakeMeetJeStad.stop(0);
    }

    /** De oude route: dezelfde klasse als in productie, wijzend naar de nep-server. */
    private MeetJeStadService liveService() {
        return new MeetJeStadService(stationRepository,
                "http://127.0.0.1:" + fakeMeetJeStad.getAddress().getPort() + "/data/?type=sensors&format=json",
                0f, 100f, 0f, 1000f, 0f, 1000f);
    }

    private static String row(long station, String timestamp, String lat, String lon,
                              String temperature, String humidity, String pm25, String pm10) {
        return String.format("{\"id\":%d,\"timestamp\":\"%s\",\"latitude\":%s,\"longitude\":%s,"
                        + "\"temperature\":%s,\"humidity\":%s,\"pm2.5\":%s,\"pm10\":%s}",
                station, timestamp, lat, lon, temperature, humidity, pm25, pm10);
    }

    /** Vergelijkbare weergave van een meting, onafhankelijk van volgorde en objectidentiteit. */
    private static List<String> describe(List<Measurement> measurements) {
        return measurements.stream()
                .sorted(Comparator.comparing((Measurement m) -> m.getStation().getStationid())
                        .thenComparing(Measurement::getTimestamp))
                .map(m -> String.join("|", String.valueOf(m.getStation().getStationid()), m.getTimestamp().toString(),
                        String.valueOf(m.getLatitude()), String.valueOf(m.getLongitude()),
                        String.valueOf(m.getTemperature()), String.valueOf(m.getHumidity()),
                        String.valueOf(m.getPm25()), String.valueOf(m.getPm10()),
                        String.valueOf(m.isLocationInherited())))
                .toList();
    }

    private static MeetJeStadParameters window(Instant start, Instant end, boolean includeFaulty) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = start;
        params.EndDate = end;
        params.includeFaultyMeasurements = includeFaulty;
        return params;
    }

    /** Doet wat de synchronisatie doet: de metingen opslaan, en dat net als in productie twee keer. */
    private void storeLikeTheSync(List<Measurement> measurements, int times) {
        for (int i = 0; i < times; i++) {
            for (Measurement source : measurements) {
                Measurement copy = new Measurement(source.getTimestamp(), source.getLatitude(), source.getLongitude(),
                        source.getTemperature(), source.getHumidity(), source.getPm25(), source.getPm10());
                copy.setLocationInherited(source.isLocationInherited());
                copy.setStation(stationRepository.getMeetstationByStationid(source.getStation().getStationid()));
                measurementRepository.save(copy);
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void localResultsEqualTheLiveResultsForTheSameData() {
        long a = createStation(null).getStationid();
        long b = createStation(null).getStationid();
        long c = createStation(null).getStationid();
        // Station a: normale metingen, een heruitgezonden dubbele rij, en een rij zonder GPS (erft positie).
        // Station b: een uitschieter in temperatuur en een onmogelijke luchtvochtigheid.
        // Station c: een enkele meting, tevens de dichtstbijzijnde bij NOW.
        fixtureJson = "[" + String.join(",",
                row(a, "2026-03-01 10:00:00", "51.56", "5.08", "14.0", "60", "8", "12"),
                row(a, "2026-03-01 12:00:00", "51.56", "5.08", "15.5", "58", "9", "13"),
                row(a, "2026-03-01 12:00:00", "51.56", "5.08", "15.5", "58", "null", "13"),
                row(a, "2026-03-01 12:20:00", "0", "0", "15.9", "57", "9", "13"),
                row(b, "2026-03-01 11:00:00", "51.57", "5.09", "14.5", "55", "7", "10"),
                row(b, "2026-03-01 12:10:00", "51.57", "5.09", "45.0", "150", "7", "10"),
                row(c, "2026-03-01 12:29:00", "51.58", "5.10", "15.0", "56", "6", "9"),
                // Buiten Tilburg: de live-route laat deze weg, dus hij komt ook niet in de database.
                row(c, "2026-03-01 12:25:00", "52.50", "4.50", "15.0", "56", "6", "9")) + "]";
        MeetJeStadService live = liveService();
        Instant start = NOW.minus(Duration.ofMinutes(1440));

        storeLikeTheSync(live.getMeasurements(window(start, NOW, true)), 2);

        for (boolean includeFaulty : new boolean[] {true, false}) {
            List<Measurement> expected = liveService().getMeasurements(window(start, NOW, includeFaulty));
            List<Measurement> actual = reader.getMeasurements(start, NOW, includeFaulty);

            assertThat(describe(actual)).as("includeFaulty=" + includeFaulty).isEqualTo(describe(expected));
            assertThat(describe(MeasurementLogic.filterClosestMeasurements(actual, NOW)))
                    .as("dichtstbijzijnde, includeFaulty=" + includeFaulty)
                    .isEqualTo(describe(MeasurementLogic.filterClosestMeasurements(expected, NOW)));
        }
        // De fixture bevat echt iets om te vergelijken, en het uitschieterfilter heeft iets gedaan.
        List<Measurement> filtered = reader.getMeasurements(start, NOW, false);
        assertThat(filtered).hasSize(6);
        assertThat(filtered).anyMatch(m -> m.getTemperature() == null && m.getStation().getStationid() == b);
        assertThat(filtered).anyMatch(m -> m.isLocationInherited());
    }

    @Test
    void measurementsOutsideTheWindowAreLeftOut() {
        long a = createStation(null).getStationid();
        fixtureJson = "[" + String.join(",",
                row(a, "2026-02-27 12:00:00", "51.56", "5.08", "14.0", "60", "8", "12"),
                row(a, "2026-03-01 12:00:00", "51.56", "5.08", "15.0", "60", "8", "12"),
                row(a, "2026-03-01 13:00:00", "51.56", "5.08", "16.0", "60", "8", "12")) + "]";
        storeLikeTheSync(liveService().getMeasurements(window(Instant.parse("2026-02-27T00:00:00Z"),
                Instant.parse("2026-03-02T00:00:00Z"), true)), 1);

        List<Measurement> result = reader.getMeasurements(NOW.minus(Duration.ofMinutes(1440)), NOW, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTemperature()).isEqualTo(15.0f);
    }

    @Test
    void anEmptyDatabaseGivesAnEmptyResultInsteadOfAnError() {
        assertThat(reader.getMeasurements(NOW.minus(Duration.ofMinutes(1440)), NOW, false)).isEmpty();
    }

    @Test
    void theNumberOfQueriesDoesNotGrowWithStationsOrMeasurements() {
        long small = queriesForFixture(3, 2);
        long large = queriesForFixture(40, 10);

        assertThat(large).as("queries voor 400 metingen/40 stations").isEqualTo(small);
        // Eén query voor de metingen en één voor de stations.
        assertThat(small).isLessThanOrEqualTo(2);
    }

    private long queriesForFixture(int stations, int measurementsPerStation) {
        List<Measurement> fixture = new ArrayList<>();
        for (int s = 0; s < stations; s++) {
            Station station = createStation(null);
            for (int m = 0; m < measurementsPerStation; m++) {
                Measurement measurement = new Measurement(NOW.minus(Duration.ofMinutes(10L * m + 1)),
                        51.56f, 5.08f, 15f, 60f, 8f, 12f);
                measurement.setStation(station);
                fixture.add(measurement);
            }
        }
        storeLikeTheSync(fixture, 1);

        StatementCounter.STATEMENTS.get().clear();
        List<Measurement> result = reader.getMeasurements(NOW.minus(Duration.ofMinutes(1440)), NOW, false);

        assertThat(result.size()).isGreaterThanOrEqualTo(stations * measurementsPerStation);
        return StatementCounter.STATEMENTS.get().size();
    }
}
