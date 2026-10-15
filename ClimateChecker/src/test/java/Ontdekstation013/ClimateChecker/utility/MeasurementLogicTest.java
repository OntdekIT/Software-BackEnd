package Ontdekstation013.ClimateChecker.utility;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    // --- Mediaan ------------------------------------------------------------

    @Test
    public void medianOfOddNumberOfValuesIsTheMiddleValue() {
        assertEquals(20.0f, MeasurementLogic.median(List.of(30.0f, 10.0f, 20.0f)));
    }

    @Test
    public void medianOfEvenNumberOfValuesIsTheAverageOfTheTwoMiddleValues() {
        // Gesorteerd: 10, 20, 30, 40 -> (20 + 30) / 2
        assertEquals(25.0f, MeasurementLogic.median(List.of(40.0f, 10.0f, 30.0f, 20.0f)));
    }

    @Test
    public void medianOfASingleValueIsThatValue() {
        assertEquals(12.5f, MeasurementLogic.median(List.of(12.5f)));
    }

    @Test
    public void medianIgnoresNullsAndNaN() {
        List<Float> values = new ArrayList<>();
        values.add(10.0f);
        values.add(null);
        values.add(Float.NaN);
        values.add(30.0f);
        values.add(20.0f);

        // Na filteren blijven 10, 20, 30 over; de mediaan daarvan is 20.
        assertEquals(20.0f, MeasurementLogic.median(values));
    }

    @Test
    public void medianOfEmptyOrNullInputIsNull() {
        assertNull(MeasurementLogic.median(new ArrayList<>()));
        assertNull(MeasurementLogic.median(null));

        List<Float> onlyNulls = new ArrayList<>();
        onlyNulls.add(null);
        assertNull(MeasurementLogic.median(onlyNulls));
    }

    @Test
    public void medianDoesNotModifyTheInputCollection() {
        List<Float> values = new ArrayList<>(List.of(30.0f, 10.0f, 20.0f));

        MeasurementLogic.median(values);

        assertEquals(List.of(30.0f, 10.0f, 20.0f), values);
    }

    @Test
    public void medianIsNotTheSameAsTheAverageWhenValuesAreSkewed() {
        // Eén uitschieter trekt het gemiddelde omhoog (25.0) maar de mediaan niet.
        // Dat is precies waarom de mediaan als apart paneel is gevraagd.
        List<Float> values = List.of(10.0f, 11.0f, 12.0f, 13.0f, 79.0f);

        assertEquals(12.0f, MeasurementLogic.median(values));
    }

    // --- Netwerkbuckets -----------------------------------------------------

    private static Measurement stationMeasurement(long stationId, Instant timestamp, Float temperature) {
        Measurement measurement = new Measurement(timestamp, 51.55f, 5f, temperature, null, null, null);
        Station station = new Station();
        station.setStationid(stationId);
        measurement.setStation(station);
        return measurement;
    }

    @Test
    public void networkBucketReportsMinMaxMedianOverAllStations() {
        Instant day = Instant.parse("2026-01-01T12:00:00Z");
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(stationMeasurement(1L, day, 10.0f));
        measurements.add(stationMeasurement(2L, day.plusSeconds(60), 20.0f));
        measurements.add(stationMeasurement(3L, day.plusSeconds(120), 30.0f));

        List<NetworkMeasurementResponse> result = MeasurementLogic.splitIntoNetworkDayMeasurements(measurements);

        assertEquals(1, result.size());
        NetworkMeasurementResponse bucket = result.get(0);
        assertEquals("01-01", bucket.getTimestamp());
        assertEquals(10.0f, bucket.getMinTemp());
        assertEquals(30.0f, bucket.getMaxTemp());
        assertEquals(20.0f, bucket.getMedianTemp());
        assertEquals(20.0f, bucket.getAvgTemp());
        assertEquals(3, bucket.getStationCount());
        assertEquals(3, bucket.getMeasurementCount());
    }

    @Test
    public void networkBucketCountsEachStationOnce() {
        Instant day = Instant.parse("2026-01-01T12:00:00Z");
        List<Measurement> measurements = new ArrayList<>();
        // Twee stations, maar station 1 levert drie metingen op dezelfde dag.
        measurements.add(stationMeasurement(1L, day, 10.0f));
        measurements.add(stationMeasurement(1L, day.plusSeconds(60), 12.0f));
        measurements.add(stationMeasurement(1L, day.plusSeconds(120), 14.0f));
        measurements.add(stationMeasurement(2L, day.plusSeconds(180), 20.0f));

        List<NetworkMeasurementResponse> result = MeasurementLogic.splitIntoNetworkDayMeasurements(measurements);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getStationCount());
        assertEquals(4, result.get(0).getMeasurementCount());
    }

    @Test
    public void networkBucketsAreChronologicalRegardlessOfArrivalOrder() {
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(stationMeasurement(1L, Instant.parse("2026-01-03T12:00:00Z"), 22.0f));
        measurements.add(stationMeasurement(1L, Instant.parse("2026-01-01T12:00:00Z"), 20.0f));
        measurements.add(stationMeasurement(2L, Instant.parse("2026-01-02T12:00:00Z"), 21.0f));

        List<NetworkMeasurementResponse> result = MeasurementLogic.splitIntoNetworkDayMeasurements(measurements);

        assertEquals(3, result.size());
        assertEquals("01-01", result.get(0).getTimestamp());
        assertEquals("02-01", result.get(1).getTimestamp());
        assertEquals("03-01", result.get(2).getTimestamp());
    }

    @Test
    public void networkBucketSkipsMeasurementsWithoutTemperature() {
        Instant day = Instant.parse("2026-01-01T12:00:00Z");
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(stationMeasurement(1L, day, 10.0f));
        measurements.add(stationMeasurement(2L, day.plusSeconds(60), null));

        List<NetworkMeasurementResponse> result = MeasurementLogic.splitIntoNetworkDayMeasurements(measurements);

        assertEquals(1, result.size());
        // Station 2 heeft die dag geen temperatuur geleverd en telt dus niet mee.
        assertEquals(1, result.get(0).getStationCount());
        assertEquals(1, result.get(0).getMeasurementCount());
        assertEquals(10.0f, result.get(0).getMedianTemp());
    }

    @Test
    public void networkBucketHandlesMeasurementWithoutStation() {
        Instant day = Instant.parse("2026-01-01T12:00:00Z");
        Measurement orphan = new Measurement(day, 51.55f, 5f, 15.0f, null, null, null);

        List<Measurement> measurements = new ArrayList<>();
        measurements.add(orphan);

        List<NetworkMeasurementResponse> result =
                assertDoesNotThrow(() -> MeasurementLogic.splitIntoNetworkDayMeasurements(measurements));

        assertEquals(1, result.size());
        assertEquals(15.0f, result.get(0).getMedianTemp());
        // De meting telt wel mee in de statistiek, maar niet in het aantal stations.
        assertEquals(0, result.get(0).getStationCount());
        assertEquals(1, result.get(0).getMeasurementCount());
    }

    @Test
    public void networkBucketsAreEmptyForEmptyInput() {
        assertFalse(MeasurementLogic.splitIntoNetworkDayMeasurements(new ArrayList<>()).iterator().hasNext());
    }

    @Test
    public void regionBucketsNowIncludeAMedian() {
        Instant hour = Instant.parse("2026-01-01T12:00:00Z");
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(stationMeasurement(1L, hour, 10.0f));
        measurements.add(stationMeasurement(2L, hour.plusSeconds(60), 11.0f));
        measurements.add(stationMeasurement(3L, hour.plusSeconds(120), 60.0f));

        List<RegionAverageBucketResponse> result =
                MeasurementLogic.splitIntoRegionBuckets(measurements, "hour");

        assertEquals(1, result.size());
        assertEquals(11.0f, result.get(0).getMedian());
        assertEquals(27.0f, result.get(0).getAvg());
    }
}
