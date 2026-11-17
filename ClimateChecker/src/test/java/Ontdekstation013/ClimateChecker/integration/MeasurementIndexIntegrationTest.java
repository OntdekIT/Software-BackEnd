package Ontdekstation013.ClimateChecker.integration;

import static org.assertj.core.api.Assertions.assertThat;

import Ontdekstation013.ClimateChecker.features.measurement.LocalMeasurementReader;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Draait de echte Flyway-migraties op een echte MariaDB en controleert de index op
 * measurement(stationid, timestamp) met EXPLAIN, en dat de tijdvenster-query van de
 * kaartendpoints daar werkt. Wordt overgeslagen als er geen Docker is.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.config.import=file:src/main/resources/application.properties",
        "spring.profiles.active=test",
        "rate.limit.enabled=false"
})
class MeasurementIndexIntegrationTest {

    @Container
    static final MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>("mariadb:11.2")
            .withDatabaseName("ontdekstation013")
            .withUsername("root")
            .withPassword("");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MARIA_DB::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "");
        registry.add("SPRING_DATASOURCE_URL", MARIA_DB::getJdbcUrl);
        registry.add("spring.flyway.url", MARIA_DB::getJdbcUrl);
        registry.add("spring.flyway.user", () -> "root");
        registry.add("spring.flyway.password", () -> "");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MariaDBDialect");
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        registry.add("management.health.mail.enabled", () -> "false");
        registry.add("application.root-user.first-name", () -> "Admin");
        registry.add("application.root-user.last-name", () -> "Beheerder");
        registry.add("application.root-user.email", () -> "admin@example.com");
        registry.add("application.root-user.password", () -> "Admin123!");
    }

    @MockBean
    private EmailSenderService emailSenderService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private LocalMeasurementReader reader;

    @Test
    void migrationCreatesTheCompositeIndexInTheRightColumnOrder() {
        List<String> columns = jdbc.queryForList(
                "SELECT COLUMN_NAME FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'measurement' "
                        + "AND INDEX_NAME = 'idx_measurement_stationid_timestamp' ORDER BY SEQ_IN_INDEX",
                String.class);

        assertThat(columns).containsExactly("stationid", "timestamp");
    }

    @Test
    void explainShowsTheIndexesAreUsed() {
        Instant now = Instant.parse("2026-03-01T12:30:00Z");
        for (int station = 1; station <= 20; station++) {
            jdbc.update("INSERT INTO station (stationid, name, registration_code, is_active, is_public) "
                    + "VALUES (?, ?, ?, 1, 1)", station, "S" + station, 1000 + station);
        }
        List<Object[]> batch = new ArrayList<>();
        for (int i = 0; i < 6000; i++) {
            long stationId = 1 + (i % 20);
            batch.add(new Object[] {stationId, Timestamp.from(now.minus(Duration.ofMinutes(i * 3L))), 51.56f, 5.08f, 15f});
        }
        jdbc.batchUpdate("INSERT INTO measurement (stationid, timestamp, latitude, longitude, temperature) "
                + "VALUES (?, ?, ?, ?, ?)", batch);
        jdbc.execute("ANALYZE TABLE measurement");

        Map<String, Object> perStation = jdbc.queryForMap(
                "EXPLAIN SELECT * FROM measurement WHERE stationid = 7 "
                        + "AND timestamp BETWEEN '2026-02-28 12:30:00' AND '2026-03-01 12:30:00'");
        assertThat(perStation.get("key")).isEqualTo("idx_measurement_stationid_timestamp");

        Map<String, Object> window = jdbc.queryForMap(
                "EXPLAIN SELECT * FROM measurement WHERE timestamp BETWEEN '2026-03-01 11:30:00' AND '2026-03-01 12:30:00'");
        assertThat(window.get("key")).isEqualTo("idx_measurement_timestamp");
    }

    @Test
    void theWindowQueryRunsAgainstMariaDbAndReturnsTheRowsInTheWindow() {
        jdbc.update("INSERT INTO station (stationid, name, registration_code, is_active, is_public) "
                + "VALUES (900, 'Venster', 9900, 1, 1)");
        jdbc.update("INSERT INTO measurement (stationid, timestamp, latitude, longitude, temperature) VALUES "
                + "(900, '2026-03-01 12:00:00', 51.56, 5.08, 15.0), "
                + "(900, '2026-03-01 12:00:00', 51.56, 5.08, 15.0), "   // dubbel, zoals de synchronisatie die maakt
                + "(900, '2026-02-20 12:00:00', 51.56, 5.08, 10.0)");     // buiten het venster

        Instant now = Instant.parse("2026-03-01T12:30:00Z");
        List<Measurement> result = reader.getMeasurements(now.minus(Duration.ofMinutes(1440)), now, false);

        assertThat(result).filteredOn(m -> m.getStation().getStationid() == 900L).hasSize(1);
    }
}
