package Ontdekstation013.ClimateChecker.ingest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Instellingen van de ingest-worker (prefix {@code ingest}). Alle waarden zijn per omgeving te
 * overschrijven met een property of env-var (bijvoorbeeld INGEST_INTERVAL_SECONDS).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ingest")
public class IngestProperties {

    /** Kill switch: bij false start de worker niet en wordt meetjestad.net nergens aangeroepen. */
    private boolean enabled = true;

    /** Tijd tussen twee succesvolle runs. */
    private long intervalSeconds = 300;

    /** Overlap met het vorige venster, zodat te laat binnengekomen metingen alsnog meekomen. */
    private long overlapMinutes = 10;

    /** Venster van de allereerste run als de database nog leeg is. */
    private long initialWindowHours = 24;

    /** Maximale inhaalperiode na een lange stilstand. */
    private long maxCatchUpHours = 168;

    /** Minimale tijd tussen twee verzoeken aan de bron (eerlijk gebruik). */
    private long minRequestIntervalSeconds = 60;

    /** Maximaal aantal verzoeken per uur aan de bron. */
    private int maxRequestsPerHour = 30;

    /** Eerste wachttijd na een fout; verdubbelt per opeenvolgende fout. */
    private long backoffInitialSeconds = 30;

    /** Bovengrens van de wachttijd na fouten. */
    private long backoffMaxSeconds = 1800;

    /** Na zoveel minuten zonder geslaagde run telt de data als verouderd (readiness: degraded). */
    private long staleAfterMinutes = 30;
}
