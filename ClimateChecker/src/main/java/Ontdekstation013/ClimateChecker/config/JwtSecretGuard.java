package Ontdekstation013.ClimateChecker.config;

import java.util.Set;
import org.springframework.core.env.Environment;

/**
 * Weigert op te starten wanneer buiten een dev/test-profiel de bekende
 * standaard JWT-sleutel (of geen sleutel) wordt gebruikt.
 */
public final class JwtSecretGuard {

    /** Staat als fallback in application.properties; mag nooit op een server draaien. */
    public static final String KNOWN_DEFAULT_SECRET =
            "ZGV2LW9ubHktc2VjcmV0LWRvLW5vdC11c2UtaW4tcHJvZHVjdGlvbi1jaGFuZ2VtZQ==";

    public static final Set<String> LOCAL_PROFILES = Set.of("dev", "test");

    private JwtSecretGuard() {
    }

    public static void verify(String secret, Environment environment) {
        if (isLocalProfileActive(environment)) {
            return;
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET is not set. Set a unique secret per environment or activate the dev/test profile.");
        }
        if (KNOWN_DEFAULT_SECRET.equals(secret.trim())) {
            throw new IllegalStateException(
                    "JWT_SECRET is the well-known default dev key. Set a unique secret per environment "
                            + "(openssl rand -base64 48) or activate the dev/test profile for local use.");
        }
    }

    private static boolean isLocalProfileActive(Environment environment) {
        for (String profile : environment.getActiveProfiles()) {
            if (LOCAL_PROFILES.contains(profile)) {
                return true;
            }
        }
        return false;
    }
}
