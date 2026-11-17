package Ontdekstation013.ClimateChecker.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

public class JwtSecretGuardTest {

    private static final String CUSTOM_SECRET =
            Base64.getEncoder().encodeToString("an-environment-specific-secret-of-32+bytes".getBytes());

    private static MockEnvironment withProfiles(String... profiles) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        return environment;
    }

    @Test
    public void customSecretIsAcceptedWithoutProfile() {
        assertDoesNotThrow(() -> JwtSecretGuard.verify(CUSTOM_SECRET, withProfiles()));
    }

    @Test
    public void defaultSecretIsRejectedWithoutProfile() {
        assertThrows(IllegalStateException.class,
                () -> JwtSecretGuard.verify(JwtSecretGuard.KNOWN_DEFAULT_SECRET, withProfiles()));
    }

    @Test
    public void defaultSecretIsRejectedInProductionProfile() {
        assertThrows(IllegalStateException.class,
                () -> JwtSecretGuard.verify(JwtSecretGuard.KNOWN_DEFAULT_SECRET, withProfiles("docker", "prod")));
    }

    @Test
    public void missingOrBlankSecretIsRejectedWithoutProfile() {
        assertThrows(IllegalStateException.class, () -> JwtSecretGuard.verify(null, withProfiles()));
        assertThrows(IllegalStateException.class, () -> JwtSecretGuard.verify("  ", withProfiles()));
    }

    @Test
    public void defaultSecretIsAllowedInDevAndTestProfile() {
        assertDoesNotThrow(() -> JwtSecretGuard.verify(JwtSecretGuard.KNOWN_DEFAULT_SECRET, withProfiles("dev")));
        assertDoesNotThrow(() -> JwtSecretGuard.verify(JwtSecretGuard.KNOWN_DEFAULT_SECRET, withProfiles("docker", "test")));
    }

    @Test
    public void defaultSecretMatchesApplicationProperties() throws Exception {
        java.nio.file.Path main = java.nio.file.Path.of("src/main/resources/application.properties");
        org.junit.jupiter.api.Assertions.assertTrue(
                java.nio.file.Files.readString(main).contains(JwtSecretGuard.KNOWN_DEFAULT_SECRET));
    }
}
