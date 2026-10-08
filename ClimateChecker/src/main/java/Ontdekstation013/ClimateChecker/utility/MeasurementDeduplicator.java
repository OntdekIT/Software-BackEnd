package Ontdekstation013.ClimateChecker.utility;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Removes duplicate measurements before they reach the aggregation logic.
 *
 * <p>The MeetJeStad source can return more than one row for the exact same
 * station at the exact same moment (a resend, a late "nagestuurde" copy, or a
 * replay). Because the backend does not persist measurements and aggregates
 * them in-memory on every request, a duplicate that slips through gets counted
 * twice in every average, minimum and maximum. This class collapses those
 * duplicates to a single record keyed on (station, timestamp).
 *
 * <p>When two records share the same key, the more complete one wins: a record
 * that reports a value for a metric is preferred over one that leaves it null,
 * so a corrected or back-filled resend does not throw away data that the first
 * copy was missing.
 */
public final class MeasurementDeduplicator {

    private MeasurementDeduplicator() {
    }

    /**
     * Returns a new list with at most one measurement per
     * (stationId, timestamp) pair. Input order is otherwise preserved.
     * Measurements without a station or timestamp are kept as-is, since they
     * cannot be keyed and dropping them would hide data.
     */
    public static List<Measurement> deduplicate(List<Measurement> measurements) {
        if (measurements == null || measurements.isEmpty()) {
            return new ArrayList<>();
        }

        Map<DuplicateKey, Measurement> uniqueByKey = new LinkedHashMap<>();
        List<Measurement> unkeyable = new ArrayList<>();

        for (Measurement measurement : measurements) {
            DuplicateKey key = keyOf(measurement);
            if (key == null) {
                unkeyable.add(measurement);
                continue;
            }

            Measurement existing = uniqueByKey.get(key);
            if (existing == null || isMoreComplete(measurement, existing)) {
                uniqueByKey.put(key, measurement);
            }
        }

        List<Measurement> result = new ArrayList<>(uniqueByKey.values());
        result.addAll(unkeyable);
        return result;
    }

    private static DuplicateKey keyOf(Measurement measurement) {
        if (measurement.getStation() == null || measurement.getStation().getStationid() == null) {
            return null;
        }
        if (measurement.getTimestamp() == null) {
            return null;
        }
        return new DuplicateKey(measurement.getStation().getStationid(), measurement.getTimestamp());
    }

    /**
     * A candidate is more complete when it reports strictly more non-null
     * metrics than the record already kept. Ties keep the existing record so
     * the first occurrence wins and the result stays deterministic.
     */
    private static boolean isMoreComplete(Measurement candidate, Measurement existing) {
        return reportedMetricCount(candidate) > reportedMetricCount(existing);
    }

    private static int reportedMetricCount(Measurement measurement) {
        int count = 0;
        if (measurement.getTemperature() != null) count++;
        if (measurement.getHumidity() != null) count++;
        if (measurement.getPm25() != null) count++;
        if (measurement.getPm10() != null) count++;
        return count;
    }

    private record DuplicateKey(Long stationId, Instant timestamp) {
        private DuplicateKey {
            Objects.requireNonNull(stationId);
            Objects.requireNonNull(timestamp);
        }
    }

    /**
     * Comparator that orders measurements by timestamp ascending. Useful for
     * callers that need late-arriving measurements placed in chronological
     * order regardless of the order the source returned them in.
     */
    public static final Comparator<Measurement> BY_TIMESTAMP =
            Comparator.comparing(Measurement::getTimestamp);
}
