package Ontdekstation013.ClimateChecker.cache;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Rondt tijdstippen af op een hele minuut. De kaartendpoints krijgen van de
 * frontend een timestamp in milliseconden (steeds "nu"), waardoor elke aanroep
 * uniek was en een cache nooit raakte. Door zowel de cachesleutel als de
 * berekening op de afgeronde minuut te baseren is het antwoord voor alle
 * aanroepen binnen dezelfde minuut identiek.
 */
public final class TimeBuckets {

    private TimeBuckets() {
    }

    public static Instant floorToMinute(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MINUTES);
    }

    /** Cachesleutel voor een time-lapse: begin, eind (beide op de minuut) en aantal stappen. */
    public static String timelapseKey(Instant from, Instant to, int steps) {
        return floorToMinute(from) + "|" + floorToMinute(to) + "|" + steps;
    }
}
