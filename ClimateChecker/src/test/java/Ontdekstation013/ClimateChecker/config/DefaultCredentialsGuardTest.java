package Ontdekstation013.ClimateChecker.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DefaultCredentialsGuardTest {

    private static MockEnvironment productionLike() {
        return new MockEnvironment()
                .withProperty("application.trusted-ip.pepper", "a-unique-pepper-for-this-environment")
                .withProperty("spring.datasource.password", "s3cret-db-password")
                .withProperty("application.root-user.password", "An0ther-Unique-Pass");
    }

    @Test
    void uniqueSecretsAreAccepted() {
        assertDoesNotThrow(() -> DefaultCredentialsGuard.verify(productionLike()));
    }

    @Test
    void defaultPepperIsRejectedOutsideDevAndTest() {
        MockEnvironment environment = productionLike()
                .withProperty("application.trusted-ip.pepper", DefaultCredentialsGuard.KNOWN_DEFAULT_PEPPER);
        assertThrows(IllegalStateException.class, () -> DefaultCredentialsGuard.verify(environment));
    }

    @Test
    void missingPepperIsRejected() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.password", "s3cret-db-password");
        assertThrows(IllegalStateException.class, () -> DefaultCredentialsGuard.verify(environment));
    }

    @Test
    void defaultDatabasePasswordIsRejected() {
        MockEnvironment environment = productionLike()
                .withProperty("spring.datasource.password", DefaultCredentialsGuard.KNOWN_DEFAULT_DB_PASSWORD);
        assertThrows(IllegalStateException.class, () -> DefaultCredentialsGuard.verify(environment));
    }

    @Test
    void examplePlaceholderRootPasswordIsRejected() {
        MockEnvironment environment = productionLike().withProperty("application.root-user.password", "Change-Me");
        assertThrows(IllegalStateException.class, () -> DefaultCredentialsGuard.verify(environment));
    }

    @Test
    void defaultsAreAllowedInDevAndTestProfile() {
        MockEnvironment dev = new MockEnvironment()
                .withProperty("application.trusted-ip.pepper", DefaultCredentialsGuard.KNOWN_DEFAULT_PEPPER)
                .withProperty("spring.datasource.password", DefaultCredentialsGuard.KNOWN_DEFAULT_DB_PASSWORD);
        dev.setActiveProfiles("dev");
        assertDoesNotThrow(() -> DefaultCredentialsGuard.verify(dev));
    }

    @Test
    void defaultPepperMatchesApplicationProperties() throws Exception {
        String properties = java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/resources/application.properties"));
        org.junit.jupiter.api.Assertions.assertTrue(properties.contains(DefaultCredentialsGuard.KNOWN_DEFAULT_PEPPER));
        org.junit.jupiter.api.Assertions.assertTrue(
                properties.contains("${SPRING_DATASOURCE_PASSWORD:" + DefaultCredentialsGuard.KNOWN_DEFAULT_DB_PASSWORD + "}"));
    }
}
