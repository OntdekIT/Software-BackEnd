package Ontdekstation013.ClimateChecker.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import Ontdekstation013.ClimateChecker.snapshot.SnapshotService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Het release-image gebruikt eclipse-temurin:21-jre, zonder de module jdk.random. Daardoor faalt
 * RandomGenerator.getDefault() (L32X64MixRandom) daar, terwijl het op een volledige JDK werkt.
 * Deze tests vangen dat in de build af.
 */
class JreCompatibilityTest {

    @Test
    void theProductionConstructorBuildsAWorkerWithoutNeedingOptionalJdkModules() {
        IngestWorker worker = new IngestWorker(new IngestProperties(), new IngestState(), mock(MeasurementSource.class),
                mock(MeasurementIngestService.class), mock(SnapshotService.class),
                new MutableClock(java.time.Instant.now()), mock(DelayedRunner.class));

        assertThat(worker).isNotNull();
    }

    @Test
    void mainCodeNeverUsesRandomGeneratorFactoriesOrJdkInternalModules() throws IOException {
        Pattern forbidden = Pattern.compile("RandomGenerator\\.(getDefault|of|all)\\(|RandomGeneratorFactory|import jdk\\.");
        List<String> offenders;
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            offenders = files.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return forbidden.matcher(Files.readString(p)).find();
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .map(Path::toString).toList();
        }

        assertThat(offenders).as("bestanden die een niet-aanwezige JRE-module nodig hebben").isEmpty();
    }
}
