package Ontdekstation013.ClimateChecker.utility;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.station.Station;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MeasurementLogicTest {

    private static Measurement measurement(Instant timestamp, Float temperature,
                                           Float humidity, Float pm25, Float pm10) {
        Measurement measurement = new Measurement(timestamp, 51.55f, 5f, temperature, humidity, pm25, pm10);
        Station station = new Station();
        station.setStationid(1L);
        measurement.setStation(station);
        return measurement;
    }

    // --- Missing values -----------------------------------------------------

    @Test
    public void dayAggregationDoesNotThrowWhenHumidityIsMissing() {
        // A reading with a temperature but no humidity used to cause an NPE in
        // the humidity aggregation because the day was still reported.
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(measurement(Instant.parse("2026-01-01T12:00:00Z"), 20.0f, null, null, null));

        List<DayMeasurementResponse> result =
                assertDoesNotThrow(() -> MeasurementLogic.splitIntoDayMeasurements(measurements));

        assertEquals(1, result.size());
        assertEquals(20.0f, result.get(0).getAvgTemp());
    }

    @Test
    public void dayAggregationIgnoresMissingHumidityInAverage() {
        Instant day = Instant.parse("2026-01-01T12:00:00Z");
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(measurement(day, 20.0f, 40.0f, null, null));
        measurements.add(measurement(day.plusSeconds(60), 22.0f, null, null, null));

        List<DayMeasurementResponse> result = MeasurementLogic.splitIntoDayMeasurements(measurements);

        // Only the one reading with a humidity counts towards the average.
        assertEquals(1, result.size());
        assertEquals(40.0f, result.get(0).getAvgHum());
        assertEquals(21.0f, result.get(0).getAvgTemp());
    }

    @Test
    public void hourAggregationIgnoresMissingTemperatureInAverage() {
        Instant hour = Instant.parse("2026-01-01T12:00:00Z");
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(measurement(hour, 20.0f, 40.0f, 10.0f, 12.0f));
        measurements.add(measurement(hour.plusSeconds(60), null, 45.0f, null, null));

        List<HourMeasurementResponse> result = MeasurementLogic.splitIntoHourMeasurements(measurements);

        assertEquals(1, result.size());
        assertEquals(20.0f, result.get(0).getAvgTemp());
    }

    // --- Late (nagestuurde) measurements ------------------------------------

    @Test
    public void dayBucketsAreChronologicalRegardlessOfArrivalOrder() {
        // Later day arrives first, earlier ("nagestuurde") day arrives second.
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(measurement(Instant.parse("2026-01-03T12:00:00Z"), 22.0f, 50.0f, null, null));
        measurements.add(measurement(Instant.parse("2026-01-01T12:00:00Z"), 20.0f, 50.0f, null, null));
        measurements.add(measurement(Instant.parse("2026-01-02T12:00:00Z"), 21.0f, 50.0f, null, null));

        List<DayMeasurementResponse> result = MeasurementLogic.splitIntoDayMeasurements(measurements);

        assertEquals(3, result.size());
        assertEquals("01-01", result.get(0).getTimestamp());
        assertEquals("02-01", result.get(1).getTimestamp());
        assertEquals("03-01", result.get(2).getTimestamp());
    }

    @Test
    public void hourBucketsAreChronologicalRegardlessOfArrivalOrder() {
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(measurement(Instant.parse("2026-01-01T15:00:00Z"), 22.0f, 50.0f, 10.0f, null));
        measurements.add(measurement(Instant.parse("2026-01-01T13:00:00Z"), 20.0f, 50.0f, 10.0f, null));
        measurements.add(measurement(Instant.parse("2026-01-01T14:00:00Z"), 21.0f, 50.0f, 10.0f, null));

        List<HourMeasurementResponse> result = MeasurementLogic.splitIntoHourMeasurements(measurements);

        assertEquals(3, result.size());
        List<Float> temps = result.stream().map(HourMeasurementResponse::getAvgTemp).toList();
        // Ascending by hour means the earliest reading's temperature comes first.
        assertEquals(20.0f, temps.get(0));
        assertEquals(21.0f, temps.get(1));
        assertEquals(22.0f, temps.get(2));
    }

    @Test
    public void emptyInputProducesNoBuckets() {
        assertFalse(MeasurementLogic.splitIntoDayMeasurements(new ArrayList<>()).iterator().hasNext());
        assertFalse(MeasurementLogic.splitIntoHourMeasurements(new ArrayList<>()).iterator().hasNext());
    }
}
