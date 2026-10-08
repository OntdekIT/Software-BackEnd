package Ontdekstation013.ClimateChecker.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.station.Station;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MeasurementDeduplicatorTest {

    private static Station station(long id) {
        Station station = new Station();
        station.setStationid(id);
        return station;
    }

    private static Measurement measurement(long stationId, Instant timestamp,
                                           Float temperature, Float humidity, Float pm25, Float pm10) {
        Measurement measurement = new Measurement(timestamp, 51.55f, 5f, temperature, humidity, pm25, pm10);
        measurement.setStation(station(stationId));
        return measurement;
    }

    @Test
    public void collapsesDuplicateStationTimestampToOneRecord() {
        Instant moment = Instant.parse("2026-01-01T12:00:00Z");
        List<Measurement> input = new ArrayList<>();
        input.add(measurement(1, moment, 20.0f, 50.0f, 10.0f, 12.0f));
        input.add(measurement(1, moment, 20.0f, 50.0f, 10.0f, 12.0f));

        List<Measurement> result = MeasurementDeduplicator.deduplicate(input);

        assertEquals(1, result.size());
    }

    @Test
    public void keepsRecordsThatDifferByStation() {
        Instant moment = Instant.parse("2026-01-01T12:00:00Z");
        List<Measurement> input = new ArrayList<>();
        input.add(measurement(1, moment, 20.0f, 50.0f, 10.0f, 12.0f));
        input.add(measurement(2, moment, 21.0f, 55.0f, 11.0f, 13.0f));

        List<Measurement> result = MeasurementDeduplicator.deduplicate(input);

        assertEquals(2, result.size());
    }

    @Test
    public void keepsRecordsThatDifferByTimestamp() {
        List<Measurement> input = new ArrayList<>();
        input.add(measurement(1, Instant.parse("2026-01-01T12:00:00Z"), 20.0f, 50.0f, 10.0f, 12.0f));
        input.add(measurement(1, Instant.parse("2026-01-01T12:10:00Z"), 21.0f, 55.0f, 11.0f, 13.0f));

        List<Measurement> result = MeasurementDeduplicator.deduplicate(input);

        assertEquals(2, result.size());
    }

    @Test
    public void prefersTheMoreCompleteDuplicate() {
        Instant moment = Instant.parse("2026-01-01T12:00:00Z");
        Measurement partial = measurement(1, moment, 20.0f, null, null, null);
        Measurement complete = measurement(1, moment, 20.0f, 50.0f, 10.0f, 12.0f);

        List<Measurement> input = new ArrayList<>();
        input.add(partial);
        input.add(complete);

        List<Measurement> result = MeasurementDeduplicator.deduplicate(input);

        assertEquals(1, result.size());
        assertSame(complete, result.get(0));
    }

    @Test
    public void keepsFirstWhenDuplicatesAreEquallyComplete() {
        Instant moment = Instant.parse("2026-01-01T12:00:00Z");
        Measurement first = measurement(1, moment, 20.0f, 50.0f, 10.0f, 12.0f);
        Measurement second = measurement(1, moment, 99.0f, 99.0f, 99.0f, 99.0f);

        List<Measurement> input = new ArrayList<>();
        input.add(first);
        input.add(second);

        List<Measurement> result = MeasurementDeduplicator.deduplicate(input);

        assertEquals(1, result.size());
        assertSame(first, result.get(0));
    }

    @Test
    public void keepsMeasurementsThatCannotBeKeyed() {
        Instant moment = Instant.parse("2026-01-01T12:00:00Z");
        Measurement noStation = new Measurement(moment, 51.55f, 5f, 20.0f, 50.0f, 10.0f, 12.0f);
        Measurement noTimestamp = measurement(1, null, 20.0f, 50.0f, 10.0f, 12.0f);

        List<Measurement> input = new ArrayList<>();
        input.add(noStation);
        input.add(noTimestamp);

        List<Measurement> result = MeasurementDeduplicator.deduplicate(input);

        assertEquals(2, result.size());
        assertTrue(result.contains(noStation));
        assertTrue(result.contains(noTimestamp));
    }

    @Test
    public void returnsEmptyListForNullOrEmptyInput() {
        assertTrue(MeasurementDeduplicator.deduplicate(null).isEmpty());
        assertTrue(MeasurementDeduplicator.deduplicate(new ArrayList<>()).isEmpty());
    }
}
