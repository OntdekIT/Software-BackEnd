package Ontdekstation013.ClimateChecker.utility;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * We use this class for measurement-related logic that is used in both MeasurementService and NeighbourhoodService
 */
public class MeasurementLogic {

    public static List<DayMeasurementResponse> splitIntoDayMeasurements(Collection<Measurement> measurements) {
        LinkedHashMap<LocalDate, Set<Measurement>> dayMeasurements = new LinkedHashMap<>();
        for (Measurement measurement : measurements) {
            if (measurement.getTemperature() != null) {
                LocalDate date = LocalDate.ofInstant(measurement.getTimestamp(), ZoneId.systemDefault());
                if (!dayMeasurements.containsKey(date)) {
                    dayMeasurements.put(date, new HashSet<>());
                }

                dayMeasurements.get(date).add(measurement);
            }
        }

        // process into DayMeasurementResponses
        //TODO make it give the ISO back
        //the entry key is the date in yyyy-MM-dd format
        //the response is an average over multiple days dus individual measurements are in correct times but they are grouped by day

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd-MM");
        List<DayMeasurementResponse> responseList = new ArrayList<>();
        for (Map.Entry<LocalDate, Set<Measurement>> entry : dayMeasurements.entrySet()) {
            DayMeasurementResponse response = new DayMeasurementResponse();

            response.setTimestamp(entry.getKey().format(pattern));
            response.setAvgTemp((float) entry.getValue()
                    .stream()
                    .mapToDouble(Measurement::getTemperature)
                    .average()
                    .orElse(Double.NaN));
            response.setMinTemp(entry.getValue()
                    .stream()
                    .map(Measurement::getTemperature)
                    .min(Float::compare)
                    .orElse(Float.NaN));
            response.setMaxTemp(entry.getValue()
                    .stream()
                    .map(Measurement::getTemperature)
                    .max(Float::compare)
                    .orElse(Float.NaN));
            response.setAvgHum((float) entry.getValue()
                    .stream()
                    .mapToDouble(Measurement::getHumidity)
                    .average()
                    .orElse(Double.NaN));
            response.setMinHum(entry.getValue()
                    .stream()
                    .map(Measurement::getHumidity)
                    .min(Float::compare)
                    .orElse(Float.NaN));
            response.setMaxHum(entry.getValue()
                    .stream()
                    .map(Measurement::getHumidity)
                    .max(Float::compare)
                    .orElse(Float.NaN));
            response.setAvgStof((float) entry.getValue()
                    .stream()
                    .filter(m -> m.getPm25() != null)
                    .mapToDouble(Measurement::getPm25)
                    .average()
                    .orElse(Double.NaN));

            responseList.add(response);
        }

        return responseList;
    }

    public static List<HourMeasurementResponse> splitIntoHourMeasurements(Collection<Measurement> measurements) {
        LinkedHashMap<LocalDateTime, Set<Measurement>> hourMeasurements = new LinkedHashMap<>();

        for (Measurement measurement : measurements) {
            LocalDateTime hour = LocalDateTime.ofInstant(measurement.getTimestamp(), ZoneId.systemDefault())
                    .truncatedTo(ChronoUnit.HOURS);

            if (!hourMeasurements.containsKey(hour)) {
                hourMeasurements.put(hour, new HashSet<>());
            }
            hourMeasurements.get(hour).add(measurement);
        }

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("HH:mm");
        List<HourMeasurementResponse> responseList = new ArrayList<>();

        for (Map.Entry<LocalDateTime, Set<Measurement>> entry : hourMeasurements.entrySet()) {
            HourMeasurementResponse response = new HourMeasurementResponse();
            response.setTimestamp(entry.getKey().format(pattern));

            response.setAvgTemp((float) entry.getValue().stream()
                    .filter(m -> m.getTemperature() != null)
                    .mapToDouble(Measurement::getTemperature)
                    .average()
                    .orElse(Double.NaN));

            response.setAvgPm25((float) entry.getValue().stream()
                    .filter(m -> m.getPm25() != null)
                    .mapToDouble(Measurement::getPm25)
                    .average()
                    .orElse(Double.NaN));

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * Groups measurements into time buckets (hour or day) and returns min/max/avg temperature per bucket.
     * Results are ordered by timestamp ascending.
     *
     * @param measurements source measurements (temperature nulls are skipped)
     * @param granularity  "hour" or "day" — anything else defaults to "hour"
     */
    public static List<RegionAverageBucketResponse> splitIntoRegionBuckets(Collection<Measurement> measurements, String granularity) {
        ChronoUnit bucketUnit = "day".equalsIgnoreCase(granularity) ? ChronoUnit.DAYS : ChronoUnit.HOURS;
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        LinkedHashMap<LocalDateTime, List<Float>> buckets = new LinkedHashMap<>();

        measurements.stream()
                .filter(m -> m.getTemperature() != null)
                .sorted(Comparator.comparing(Measurement::getTimestamp))
                .forEach(m -> {
                    LocalDateTime bucket = LocalDateTime.ofInstant(m.getTimestamp(), ZoneId.systemDefault())
                            .truncatedTo(bucketUnit);
                    buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(m.getTemperature());
                });

        List<RegionAverageBucketResponse> result = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<Float>> entry : buckets.entrySet()) {
            List<Float> temps = entry.getValue();
            RegionAverageBucketResponse response = new RegionAverageBucketResponse();
            response.setTimestamp(entry.getKey().format(isoFormatter));
            response.setMin(temps.stream().min(Float::compare).orElse(Float.NaN));
            response.setMax(temps.stream().max(Float::compare).orElse(Float.NaN));
            response.setAvg((float) temps.stream().mapToDouble(Float::doubleValue).average().orElse(Double.NaN));
            result.add(response);
        }
        return result;
    }

    /**
     * Filters out measurements of the same station that are further away from the given timestamp
     *
     * @return closest measurement to given dateTime for each unique station in given collection
     */
    public static List<Measurement> filterClosestMeasurements(Collection<Measurement> measurements, Instant dateTime) {
        Map<Long, Measurement> measurementMapByStation = new HashMap<>();

        for (Measurement measurement : measurements) {
            Long stationId = measurement.getStation().getStationid();

            if (!measurementMapByStation.containsKey(stationId)) {
                measurementMapByStation.put(stationId, measurement);
            } else {
                Duration existingDifference = Duration.between(dateTime, measurementMapByStation.get(stationId).getTimestamp()).abs();
                Duration newDifference = Duration.between(dateTime, measurement.getTimestamp()).abs();

                if (newDifference.compareTo(existingDifference) < 0) {
                    measurementMapByStation.put(stationId, measurement);
                }
            }
        }

        return new ArrayList<>(measurementMapByStation.values());
    }
}
