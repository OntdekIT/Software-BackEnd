package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Flyway owns the schema, Hibernate may only validate it. Guards against a compose file or
 * the main properties switching to update/create, which silently alters a live database.
 */
class SchemaManagementTest {

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    @Test
    void mainPropertiesValidateTheSchema() throws IOException {
        assertThat(read("src/main/resources/application.properties"))
                .contains("spring.jpa.hibernate.ddl-auto=validate");
    }

    @Test
    void everyComposeFileValidatesTheSchemaAndNoneUsesAnotherMode() throws IOException {
        for (String compose : List.of("docker-compose.yml", "docker-compose-dev.yml", "docker-compose-prod.yml")) {
            String content = read(compose);
            assertThat(content).as(compose).contains("SPRING_JPA_HIBERNATE_DDL_AUTO: validate");
            assertThat(content).as(compose).doesNotContain("DDL_AUTO: update").doesNotContain("DDL_AUTO: create");
        }
    }

    @Test
    void flywayStaysEnabledInTheMainConfiguration() throws IOException {
        assertThat(read("src/main/resources/application.properties")).doesNotContain("spring.flyway.enabled=false");
    }
}
