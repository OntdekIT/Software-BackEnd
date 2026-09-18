package Ontdekstation013.ClimateChecker.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Basis voor integratietests: start één echte MariaDB (via Testcontainers) die
 * Spring automatisch als datasource gebruikt. Zo draaien Flyway-migraties, de
 * JPA-mapping en de repository-queries tegen een echte database — niet tegen
 * mocks. MariaDB is op 11.2 gepind (dezelfde reden als in productie: nieuwere
 * versies breken Flyway 10.10).
 */
@Testcontainers
@SpringBootTest
public abstract class AbstractIntegrationTest {

    // De init-migratie bevat 'SET GLOBAL ...' (restant van een mysqldump) wat de
    // SUPER-rechten vereist die alleen root heeft. Daarom draaien we als root,
    // net als de docker-compose in productie.
    @Container
    static final MariaDBContainer<?> MARIA_DB =
            new MariaDBContainer<>("mariadb:11.2")
                    .withDatabaseName("ontdekstation013")
                    .withUsername("root")
                    .withPassword("");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Datasource + Flyway naar de Testcontainers-database (als root).
        registry.add("spring.datasource.url", MARIA_DB::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "");
        registry.add("SPRING_DATASOURCE_URL", MARIA_DB::getJdbcUrl);
        registry.add("spring.flyway.url", MARIA_DB::getJdbcUrl);
        registry.add("spring.flyway.user", () -> "root");
        registry.add("spring.flyway.password", () -> "");

        // De applicatie verwacht deze mail-properties; in tests hebben we geen
        // echte mailserver nodig, dus vullen we placeholders in.
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
        registry.add("MAILSERVER_RELAY_HOST", () -> "localhost");
        registry.add("MAILSERVER_RELAY_PORT", () -> "1025");
        registry.add("MAIL_USERNAME", () -> "test");
        registry.add("MAIL_PASSWORD", () -> "test");
        // Geen echte mailserver in tests -> de mail-health-indicator uitzetten.
        registry.add("management.health.mail.enabled", () -> "false");
        // Root-user seed properties.
        registry.add("application.root-user.first-name", () -> "Admin");
        registry.add("application.root-user.last-name", () -> "Beheerder");
        registry.add("application.root-user.email", () -> "admin@example.com");
        registry.add("application.root-user.password", () -> "Admin123!");
    }
}
