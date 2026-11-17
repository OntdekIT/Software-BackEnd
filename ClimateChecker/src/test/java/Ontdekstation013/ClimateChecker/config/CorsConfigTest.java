package Ontdekstation013.ClimateChecker.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

public class CorsConfigTest {

    private static class InspectableRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }

    private static List<String> originsFor(String configured) {
        InspectableRegistry registry = new InspectableRegistry();
        new CorsConfig(configured).corsConfigurer().addCorsMappings(registry);
        return registry.configurations().get("/**").getAllowedOrigins();
    }

    @Test
    public void singleOriginIsAllowed() {
        assertEquals(List.of("https://test.example.org"), originsFor("https://test.example.org"));
    }

    @Test
    public void multipleOriginsAreSplitAndTrimmed() {
        assertEquals(List.of("https://a.example.org", "https://b.example.org"),
                originsFor(" https://a.example.org , https://b.example.org "));
    }

    @Test
    public void blankEntriesAreIgnoredAndCredentialsStayEnabled() {
        InspectableRegistry registry = new InspectableRegistry();
        new CorsConfig("https://a.example.org,,").corsConfigurer().addCorsMappings(registry);
        CorsConfiguration configuration = registry.configurations().get("/**");

        assertEquals(List.of("https://a.example.org"), configuration.getAllowedOrigins());
        assertTrue(configuration.getAllowCredentials());
    }
}
