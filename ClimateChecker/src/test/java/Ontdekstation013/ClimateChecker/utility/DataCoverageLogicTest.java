package Ontdekstation013.ClimateChecker.utility;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.utility.DataCoverageLogic.Granularity;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DataCoverageLogicTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");
    private static final Instant FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-01-01T04:00:00Z"); // 4 hour buckets

    private static Station station(long id) {
        Station station = new Station();
        station.setStationid(id);
        station.setName("Station " + id);
        return station;
    }

    private static Measurement reading(Station station, String timestamp, Float pm25) {
        Measurement measurement = new Measurement(Instant.parse(timestamp), 51.55f, 5f, 12.0f, 80.0f, pm25, null);
        measurement.setStation(station);
        return measurement;
    }

    private static StationCoverageResponse stationResult(DataCoverageResponse response, long id) {
        return response.getStations().stream()
                .filter(s -> s.getStationId() == id)
                .findFirst()
                .orElseThrow();
    }

    @Test
    public void buildsOneBucketPerHourInPeriod() {
        List<Instant> buckets = DataCoverageLogic.buildBuckets(FROM, TO, Granularity.HOUR, ZONE);

        assertEquals(4, buckets.size());
        assertEquals(FROM, buckets.get(0));
        assertEquals(Instant.parse("2026-01-01T03:00:00Z"), buckets.get(3));
    }

    @Test
    public void dayBucketsFollowDutchDays() {
        // 2026-01-01T00:00Z is 01:00 in Amsterdam, so the day bucket starts at 23:00Z the day before.
        List<Instant> buckets = DataCoverageLogic.buildBuckets(FROM, Instant.parse("2026-01-02T12:00:00Z"), Granularity.DAY, ZONE);

        assertEquals(2, buckets.size());
        assertEquals(Instant.parse("2025-12-31T23:00:00Z"), buckets.get(0));
    }

    @Test
    public void stationWithPmEveryHourIsOk() {
        Station s1 = station(1);
        List<Measurement> measurements = List.of(
                reading(s1, "2026-01-01T00:10:00Z", 5f),
                reading(s1, "2026-01-01T01:10:00Z", 5f),
                reading(s1, "2026-01-01T02:10:00Z", 5f),
                reading(s1, "2026-01-01T03:10:00Z", 5f));

        DataCoverageResponse response = DataCoverageLogic.calculate(measurements, List.of(s1), FROM, TO, Granularity.HOUR, ZONE);
        StationCoverageResponse result = stationResult(response, 1);

        assertEquals(1f, result.getPmCoverage());
        assertEquals("OK", result.getStatus());
        assertEquals(0, result.getLongestPmGapBuckets());
        assertEquals(60f, result.getMedianPmIntervalMinutes());
    }

    @Test
    public void stationWithGapsIsIrregularAndReportsLongestGap() {
        Station s1 = station(1);
        List<Measurement> measurements = List.of(
                reading(s1, "2026-01-01T00:10:00Z", 5f),
                reading(s1, "2026-01-01T00:20:00Z", 5f));

        DataCoverageResponse response = DataCoverageLogic.calculate(measurements, List.of(s1), FROM, TO, Granularity.HOUR, ZONE);
        StationCoverageResponse result = stationResult(response, 1);

        assertEquals(0.25f, result.getPmCoverage());
        assertEquals("IRREGULAR", result.getStatus());
        assertEquals(3, result.getLongestPmGapBuckets());
        assertArrayEquals(new int[]{2, 0, 0, 0}, result.getPmTimeline());
    }

    @Test
    public void readingsWithoutPmMeanNoPmSensor() {
        Station s1 = station(1);
        List<Measurement> measurements = List.of(
                reading(s1, "2026-01-01T00:10:00Z", null),
                reading(s1, "2026-01-01T01:10:00Z", null));

        DataCoverageResponse response = DataCoverageLogic.calculate(measurements, List.of(s1), FROM, TO, Granularity.HOUR, ZONE);
        StationCoverageResponse result = stationResult(response, 1);

        assertEquals("NO_PM", result.getStatus());
        assertEquals(0.5f, result.getDataCoverage());
        assertEquals(0f, result.getPmCoverage());
        assertNull(result.getMedianPmIntervalMinutes());
    }

    @Test
    public void knownStationWithoutReadingsIsListedAsNoData() {
        DataCoverageResponse response = DataCoverageLogic.calculate(List.of(), List.of(station(7)), FROM, TO, Granularity.HOUR, ZONE);
        StationCoverageResponse result = stationResult(response, 7);

        assertEquals("NO_DATA", result.getStatus());
        assertEquals(4, result.getLongestPmGapBuckets());
        assertEquals(0, response.getPmCapableStationCount());
        assertNull(response.getNetworkPmCoverage());
    }

    @Test
    public void unknownStationsFromDataAreIncluded() {
        Station unknown = station(99);
        DataCoverageResponse response = DataCoverageLogic.calculate(
                List.of(reading(unknown, "2026-01-01T00:10:00Z", 5f)), List.of(), FROM, TO, Granularity.HOUR, ZONE);

        assertEquals(1, response.getStationCount());
        assertEquals(1, stationResult(response, 99).getPmReadings());
    }

    @Test
    public void readingsOutsidePeriodAreIgnored() {
        Station s1 = station(1);
        List<Measurement> measurements = List.of(
                reading(s1, "2025-12-31T23:59:00Z", 5f),
                reading(s1, "2026-01-01T04:00:00Z", 5f));

        DataCoverageResponse response = DataCoverageLogic.calculate(measurements, List.of(s1), FROM, TO, Granularity.HOUR, ZONE);

        assertEquals(0, stationResult(response, 1).getTotalReadings());
    }

    @Test
    public void simultaneousGapsAcrossStationsAreReportedAsNetworkGap() {
        Station s1 = station(1);
        Station s2 = station(2);
        List<Measurement> measurements = new ArrayList<>();
        // Both stations report in hours 0 and 3, both are silent in hours 1 and 2.
        for (Station s : List.of(s1, s2)) {
            measurements.add(reading(s, "2026-01-01T00:10:00Z", 5f));
            measurements.add(reading(s, "2026-01-01T03:10:00Z", 5f));
        }

        DataCoverageResponse response = DataCoverageLogic.calculate(measurements, List.of(s1, s2), FROM, TO, Granularity.HOUR, ZONE);

        assertEquals(List.of(1f, 0f, 0f, 1f), response.getNetworkTimeline());
        assertEquals(0.5f, response.getNetworkPmCoverage());
        assertEquals(1, response.getNetworkGaps().size());
        NetworkGapResponse gap = response.getNetworkGaps().get(0);
        assertEquals("2026-01-01T01:00:00Z", gap.getStart());
        assertEquals("2026-01-01T03:00:00Z", gap.getEnd());
        assertEquals(2, gap.getBucketCount());
    }

    @Test
    public void stationWithoutPmSensorDoesNotPullNetworkCoverageDown() {
        Station withPm = station(1);
        Station withoutPm = station(2);
        List<Measurement> measurements = new ArrayList<>();
        for (int hour = 0; hour < 4; hour++) {
            measurements.add(reading(withPm, "2026-01-01T0" + hour + ":10:00Z", 5f));
            measurements.add(reading(withoutPm, "2026-01-01T0" + hour + ":10:00Z", null));
        }

        DataCoverageResponse response = DataCoverageLogic.calculate(measurements, List.of(withPm, withoutPm), FROM, TO, Granularity.HOUR, ZONE);

        assertEquals(1, response.getPmCapableStationCount());
        assertEquals(1f, response.getNetworkPmCoverage());
        assertEquals(0, response.getNetworkGaps().size());
    }

    @Test
    public void unknownGranularityIsRejected() {
        assertThrows(InvalidArgumentException.class, () -> Granularity.parse("week"));
    }
}
