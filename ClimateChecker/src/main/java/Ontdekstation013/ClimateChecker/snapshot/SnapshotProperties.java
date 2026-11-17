package Ontdekstation013.ClimateChecker.snapshot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "snapshot")
public class SnapshotProperties {

    /** Een meting telt als actueel als ze hoogstens zoveel minuten oud is (default 2 uur). */
    private long recencyMinutes = 120;

    /** Een verzoek voor "nu" binnen deze marge wordt uit de snapshot beantwoord. */
    private long nowToleranceSeconds = 300;

    /** max-age van Cache-Control op /api/snapshot. */
    private long cacheMaxAgeSeconds = 30;
}
