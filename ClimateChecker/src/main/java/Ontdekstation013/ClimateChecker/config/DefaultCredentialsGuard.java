package Ontdekstation013.ClimateChecker.config;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Weigert op te starten wanneer buiten een dev/test-profiel bekende standaard
 * geheimen worden gebruikt: de trusted-IP pepper, het standaard databasewachtwoord
 * uit application.properties en de placeholder-wachtwoorden uit .env.example.
 * Het JWT-geheim wordt door {@link JwtSecretGuard} bewaakt.
 */
@Component
public class DefaultCredentialsGuard {

    /** Fallbacks uit application.properties. */
    public static final String KNOWN_DEFAULT_PEPPER = "dev-only-trusted-ip-pepper-change-me";
    public static final String KNOWN_DEFAULT_DB_PASSWORD = "password";

    /** Placeholder uit .env.example; mag nooit echt gebruikt worden. */
    private static final String EXAMPLE_PLACEHOLDER = "change-me";

    private static final Set<String> FORBIDDEN_PASSWORDS = Set.of(KNOWN_DEFAULT_DB_PASSWORD, EXAMPLE_PLACEHOLDER);

    private final Environment environment;

    public DefaultCredentialsGuard(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void verifyOnStartup() {
        verify(environment);
    }

    public static void verify(Environment environment) {
        if (JwtSecretGuard.isLocalProfileActive(environment)) {
            return;
        }
        String pepper = environment.getProperty("application.trusted-ip.pepper");
        if (pepper == null || pepper.isBlank() || KNOWN_DEFAULT_PEPPER.equals(pepper.trim())) {
            throw new IllegalStateException(
                    "TRUSTED_IP_PEPPER is not set or is the well-known default. Set a unique value per environment "
                            + "(openssl rand -base64 48) or activate the dev/test profile for local use.");
        }
        rejectKnownPassword("SPRING_DATASOURCE_PASSWORD", environment.getProperty("spring.datasource.password"));
        rejectKnownPassword("ROOT_USER_PASSWORD", environment.getProperty("application.root-user.password"));
    }

    private static void rejectKnownPassword(String variable, String value) {
        if (value != null && FORBIDDEN_PASSWORDS.contains(value.trim().toLowerCase())) {
            throw new IllegalStateException(
                    variable + " is a well-known default or placeholder password. Set a unique value per environment "
                            + "or activate the dev/test profile for local use.");
        }
    }
}
