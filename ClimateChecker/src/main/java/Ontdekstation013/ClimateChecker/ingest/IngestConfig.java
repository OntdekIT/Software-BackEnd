package Ontdekstation013.ClimateChecker.ingest;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({IngestProperties.class, Ontdekstation013.ClimateChecker.snapshot.SnapshotProperties.class})
public class IngestConfig {

    @Bean
    public Clock ingestClock() {
        return Clock.systemUTC();
    }

    @Bean(destroyMethod = "shutdown")
    public DelayedRunner ingestRunner() {
        return new ExecutorDelayedRunner();
    }
}
