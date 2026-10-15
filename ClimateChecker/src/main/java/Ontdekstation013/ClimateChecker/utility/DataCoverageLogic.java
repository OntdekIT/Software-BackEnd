package Ontdekstation013.ClimateChecker.utility;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.station.Station;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates how complete the particulate matter (fijnstof) data of each station
 * and of the whole network is over a period.
 *
 * <p>The period is split into fixed buckets (hours or days). A bucket counts as
 * "covered" for a station when that station delivered at least one pm2.5/pm10
 * reading in it. Counting buckets instead of individual readings keeps the
 * coverage meaningful without knowing each sensor's exact send interval.
 *
 * <p>On top of the per-station numbers this class also looks at the network as a
 * whole: when many stations miss the same bucket at the same time the cause is
 * most likely shared (data source, gateway, sync) rather than a single sensor.
 * Those moments are reported as network gaps.
 */
public final class DataCoverageLogic {

    /** A station with at least this share of covered buckets is considered reliable. */
    public static final float OK_THRESHOLD = 0.8f;

    /** Buckets where fewer than this share of pm-capable stations report are a network gap. */
    public static final float NETWORK_GAP_THRESHOLD = 0.5f;

    public enum Granularity {
        HOUR(ChronoUnit.HOURS),
        DAY(ChronoUnit.DAYS);

        private final ChronoUnit unit;

        Granularity(ChronoUnit unit) {
            this.unit = unit;
        }

        public static Granularity parse(String value) {
            if (value == null || value.isBlank() || "hour".equalsIgnoreCase(value)) {
                return HOUR;
            }
            if ("day".equalsIgnoreCase(value)) {
                return DAY;
            }
            throw new InvalidArgumentException("Granularity must be 'hour' or 'day'");
        }
    }

    public enum Status {
        /** pm coverage at or above {@link #OK_THRESHOLD}. */
        OK,
        /** Delivers pm data, but with gaps. */
        IRREGULAR,
        /** Delivers readings, but never a pm value: no or a broken particulate sensor. */
        NO_PM,
        /** Delivered nothing at all in the period. */
        NO_DATA
    }

    private DataCoverageLogic() {
    }

    /**
     * Truncates a moment to the start of its bucket in the given zone, so day
     * buckets follow local (Dutch) days instead of UTC days.
     */
    public static Instant alignToBucket(Instant moment, Granularity granularity, ZoneId zone) {
        return moment.atZone(zone).truncatedTo(granularity.unit).toInstant();
    }

    /**
     * Returns the start of every bucket in [from, to). The first bucket starts at
     * {@code from} aligned with {@link #alignToBucket}.
     */
    public static List<Instant> buildBuckets(Instant from, Instant to, Granularity granularity, ZoneId zone) {
        List<Instant> buckets = new ArrayList<>();
        ZonedDateTime bucket = alignToBucket(from, granularity, zone).atZone(zone);
        while (bucket.toInstant().isBefore(to)) {
            buckets.add(bucket.toInstant());
            bucket = bucket.plus(1, granularity.unit);
        }
        return buckets;
    }

    /**
     * @param measurements  raw readings (faulty values included) for the period
     * @param knownStations stations from the database; listed even when they sent nothing
     */
    public static DataCoverageResponse calculate(Collection<Measurement> measurements,
                                                 Collection<Station> knownStations,
                                                 Instant from, Instant to,
                                                 Granularity granularity, ZoneId zone) {
        Instant start = alignToBucket(from, granularity, zone);
        List<Instant> buckets = buildBuckets(start, to, granularity, zone);
        int bucketCount = buckets.size();

        Map<Long, StationAccumulator> accumulators = new LinkedHashMap<>();
        knownStations.stream()
                .filter(station -> station.getStationid() != null)
                .sorted(Comparator.comparing(Station::getStationid))
                .forEach(station -> accumulators.put(station.getStationid(), new StationAccumulator(station, bucketCount)));

        for (Measurement measurement : measurements) {
            if (measurement.getStation() == null || measurement.getStation().getStationid() == null
                    || measurement.getTimestamp() == null) {
                continue;
            }
            Instant timestamp = measurement.getTimestamp();
            if (timestamp.isBefore(start) || !timestamp.isBefore(to)) {
                continue;
            }
            int bucketIndex = bucketIndexOf(buckets, timestamp);
            if (bucketIndex < 0) {
                continue;
            }

            StationAccumulator accumulator = accumulators.computeIfAbsent(
                    measurement.getStation().getStationid(),
                    id -> new StationAccumulator(measurement.getStation(), bucketCount));
            accumulator.add(measurement, bucketIndex);
        }

        List<StationCoverageResponse> stations = new ArrayList<>();
        for (StationAccumulator accumulator : accumulators.values()) {
            stations.add(accumulator.toResponse(bucketCount));
        }

        DataCoverageResponse response = new DataCoverageResponse();
        response.setFrom(start.toString());
        response.setTo(to.toString());
        response.setGranularity(granularity.name().toLowerCase());
        response.setOkThreshold(OK_THRESHOLD);
        response.setNetworkGapThreshold(NETWORK_GAP_THRESHOLD);
        response.setBuckets(buckets.stream().map(Instant::toString).toList());
        response.setStations(stations);
        response.setStationCount(stations.size());
        fillNetworkFigures(response, stations, buckets, to);
        return response;
    }

    private static void fillNetworkFigures(DataCoverageResponse response, List<StationCoverageResponse> stations,
                                           List<Instant> buckets, Instant to) {
        // Only stations that sent pm data at least once say something about the
        // network's pm coverage; stations without a pm sensor would pull every
        // bucket down and hide the real gaps.
        List<StationCoverageResponse> pmCapable = stations.stream()
                .filter(station -> station.getPmReadings() > 0)
                .toList();
        response.setPmCapableStationCount(pmCapable.size());

        if (pmCapable.isEmpty() || buckets.isEmpty()) {
            response.setNetworkPmCoverage(null);
            response.setNetworkTimeline(List.of());
            response.setNetworkGaps(List.of());
            return;
        }

        List<Float> timeline = new ArrayList<>();
        double total = 0;
        for (int i = 0; i < buckets.size(); i++) {
            int reporting = 0;
            for (StationCoverageResponse station : pmCapable) {
                if (station.getPmTimeline()[i] > 0) {
                    reporting++;
                }
            }
            float share = (float) reporting / pmCapable.size();
            timeline.add(share);
            total += share;
        }
        response.setNetworkTimeline(timeline);
        response.setNetworkPmCoverage((float) (total / buckets.size()));
        response.setNetworkGaps(findNetworkGaps(timeline, buckets, to));
    }

    /** Merges consecutive buckets below {@link #NETWORK_GAP_THRESHOLD} into periods. */
    private static List<NetworkGapResponse> findNetworkGaps(List<Float> timeline, List<Instant> buckets, Instant to) {
        List<NetworkGapResponse> gaps = new ArrayList<>();
        NetworkGapResponse current = null;
        for (int i = 0; i < timeline.size(); i++) {
            float share = timeline.get(i);
            if (share < NETWORK_GAP_THRESHOLD) {
                Instant end = i + 1 < buckets.size() ? buckets.get(i + 1) : to;
                if (current == null) {
                    current = new NetworkGapResponse();
                    current.setStart(buckets.get(i).toString());
                    current.setLowestShare(share);
                    gaps.add(current);
                }
                current.setEnd(end.toString());
                current.setBucketCount(current.getBucketCount() + 1);
                current.setLowestShare(Math.min(current.getLowestShare(), share));
            } else {
                current = null;
            }
        }
        return gaps;
    }

    private static int bucketIndexOf(List<Instant> buckets, Instant timestamp) {
        int index = Collections.binarySearch(buckets, timestamp);
        // Not an exact bucket start: binarySearch returns -(insertionPoint) - 1,
        // and the bucket that contains the timestamp is the one before that point.
        return index >= 0 ? index : -index - 2;
    }

    private static final class StationAccumulator {
        private final Station station;
        private final int[] readingsPerBucket;
        private final int[] pmReadingsPerBucket;
        private final List<Instant> pmTimestamps = new ArrayList<>();
        private int totalReadings;

        private StationAccumulator(Station station, int bucketCount) {
            this.station = station;
            this.readingsPerBucket = new int[bucketCount];
            this.pmReadingsPerBucket = new int[bucketCount];
        }

        private void add(Measurement measurement, int bucketIndex) {
            totalReadings++;
            readingsPerBucket[bucketIndex]++;
            if (measurement.getPm25() != null || measurement.getPm10() != null) {
                pmReadingsPerBucket[bucketIndex]++;
                pmTimestamps.add(measurement.getTimestamp());
            }
        }

        private StationCoverageResponse toResponse(int bucketCount) {
            int bucketsWithData = countCovered(readingsPerBucket);
            int bucketsWithPm = countCovered(pmReadingsPerBucket);

            StationCoverageResponse response = new StationCoverageResponse();
            response.setStationId(station.getStationid());
            response.setName(station.getName());
            response.setActive(station.getIsActive());
            response.setStofError(station.getStofError());
            response.setTotalReadings(totalReadings);
            response.setPmReadings(pmTimestamps.size());
            response.setBucketsWithData(bucketsWithData);
            response.setBucketsWithPm(bucketsWithPm);
            response.setDataCoverage(bucketCount == 0 ? 0f : (float) bucketsWithData / bucketCount);
            response.setPmCoverage(bucketCount == 0 ? 0f : (float) bucketsWithPm / bucketCount);
            response.setLongestPmGapBuckets(longestRunOfZeros(pmReadingsPerBucket));
            response.setMedianPmIntervalMinutes(medianIntervalMinutes(pmTimestamps));
            response.setPmTimeline(pmReadingsPerBucket);
            response.setStatus(statusOf(response).name());
            return response;
        }
    }

    private static Status statusOf(StationCoverageResponse station) {
        if (station.getTotalReadings() == 0) {
            return Status.NO_DATA;
        }
        if (station.getPmReadings() == 0) {
            return Status.NO_PM;
        }
        return station.getPmCoverage() >= OK_THRESHOLD ? Status.OK : Status.IRREGULAR;
    }

    private static int countCovered(int[] perBucket) {
        int covered = 0;
        for (int count : perBucket) {
            if (count > 0) {
                covered++;
            }
        }
        return covered;
    }

    static int longestRunOfZeros(int[] perBucket) {
        int longest = 0;
        int current = 0;
        for (int count : perBucket) {
            current = count == 0 ? current + 1 : 0;
            longest = Math.max(longest, current);
        }
        return longest;
    }

    /**
     * Median time between consecutive pm readings. Gives an idea of the send
     * interval a station normally keeps, so a long gap can be read against it.
     */
    static Float medianIntervalMinutes(List<Instant> timestamps) {
        if (timestamps.size() < 2) {
            return null;
        }
        List<Instant> sorted = new ArrayList<>(timestamps);
        Collections.sort(sorted);
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < sorted.size(); i++) {
            intervals.add(Duration.between(sorted.get(i - 1), sorted.get(i)).getSeconds());
        }
        Collections.sort(intervals);
        int middle = intervals.size() / 2;
        double medianSeconds = intervals.size() % 2 == 1
                ? intervals.get(middle)
                : (intervals.get(middle - 1) + intervals.get(middle)) / 2.0;
        return (float) (medianSeconds / 60.0);
    }
}
