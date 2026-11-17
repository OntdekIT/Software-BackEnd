package Ontdekstation013.ClimateChecker.config;

import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the configured application time zone ({@code application.timezone}, default UTC)
 * so dates are interpreted and bucketed in that zone and not in whatever zone the JVM
 * or the host happens to use.
 */
@Configuration
public class TimeZoneConfig {

    @Bean
    public ZoneId applicationZone(@Value("${application.timezone:UTC}") String timezone) {
        return ZoneId.of(timezone);
    }
}
