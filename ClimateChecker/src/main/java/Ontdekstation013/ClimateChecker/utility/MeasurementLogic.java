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
        // A day is only reported when at least one reading has a temperature,
        // but within that day each metric is aggregated independently: a
        // reading that is missing humidity or particulate must not crash the
        // humidity/pm aggregation nor skew it, so every metric filters its own
        // nulls (see splitIntoHourMeasurements for the same pattern).
        //
        // A TreeMap keeps the day buckets in chronological order no matter the
        // order the source returned readings in, so a late ("nagestuurde")
        // measurement for an earlier day still appears before later days in the
        // response instead of being appended at the end.
        TreeMap<LocalDate, Set<Measurement>> dayMeasurements = new TreeMap<>();
        for (Measurement measurement : measurements) {
            if (measurement.getTemperature() != null) {
                LocalDate date = LocalDate.ofInstant(measurement.getTimestamp(), ZoneId.systemDefault());
                if (!dayMeasurements.containsKey(date)) {
                    dayMeasurements.put(date, new HashSet<>());
                }
                dayMeasurements.get(date).add(measurement);
            }
        }

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd-MM");
        List<DayMeasurementResponse> responseList = new ArrayList<>();
        for (Map.Entry<LocalDate, Set<Measurement>> entry : dayMeasurements.entrySet()) {
            DayMeasurementResponse response = new DayMeasurementResponse();

            response.setTimestamp(entry.getKey().format(pattern));
            response.setAvgTemp((float) entry.getValue()
                    .stream()
                    .filter(m -> m.getTemperature() != null)
                    .mapToDouble(Measurement::getTemperature)
                    .average()
                    .orElse(Double.NaN));
            response.setMinTemp(entry.getValue()
                    .stream()
                    .map(Measurement::getTemperature)
                    .filter(Objects::nonNull)
                    .min(Float::compare)
                    .orElse(Float.NaN));
            response.setMaxTemp(entry.getValue()
                    .stream()
                    .map(Measurement::getTemperature)
                    .filter(Objects::nonNull)
                    .max(Float::compare)
                    .orElse(Float.NaN));
            response.setAvgHum((float) entry.getValue()
                    .stream()
                    .filter(m -> m.getHumidity() != null)
                    .mapToDouble(Measurement::getHumidity)
                    .average()
                    .orElse(Double.NaN));
            response.setMinHum(entry.getValue()
                    .stream()
                    .map(Measurement::getHumidity)
                    .filter(Objects::nonNull)
                    .min(Float::compare)
                    .orElse(Float.NaN));
            response.setMaxHum(entry.getValue()
                    .stream()
                    .map(Measurement::getHumidity)
                    .filter(Objects::nonNull)
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

    /**
     * Groepeert de metingen van het hele netwerk per dag en berekent per dag het
     * minimum, het maximum, de mediaan en het gemiddelde over alle stations samen.
     *
     * <p>Alle vier de waarden komen uit dezelfde verzameling metingen van die dag.
     * Het minimum is dus de koudste meting die ergens in het netwerk is gedaan, en
     * de mediaan de middelste meting — niet de mediaan van de stationsgemiddelden.
     * Die keuze houdt de vier waarden onderling consistent.
     *
     * <p>Een dag zonder enkele temperatuurmeting levert geen bucket op, zodat de
     * grafiek geen lege punten toont. Een TreeMap houdt de dagen chronologisch,
     * ook als er later nog metingen voor een eerdere dag binnenkomen.
     *
     * @param measurements metingen van alle stations (temperatuur-nulls worden overgeslagen)
     */
    public static List<NetworkMeasurementResponse> splitIntoNetworkDayMeasurements(Collection<Measurement> measurements) {
        TreeMap<LocalDate, List<Float>> temperaturesPerDay = new TreeMap<>();
        TreeMap<LocalDate, Set<Long>> stationsPerDay = new TreeMap<>();

        for (Measurement measurement : measurements) {
            if (measurement.getTemperature() == null) {
                continue;
            }

            LocalDate date = LocalDate.ofInstant(measurement.getTimestamp(), ZoneId.systemDefault());
            temperaturesPerDay.computeIfAbsent(date, k -> new ArrayList<>()).add(measurement.getTemperature());

            // Het station kan ontbreken wanneer een meting niet aan een bekend
            // station te koppelen is; die meting telt dan wel mee in de
            // statistiek, maar niet in het aantal stations.
            if (measurement.getStation() != null && measurement.getStation().getStationid() != null) {
                stationsPerDay.computeIfAbsent(date, k -> new HashSet<>())
                        .add(measurement.getStation().getStationid());
            }
        }

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd-MM");
        List<NetworkMeasurementResponse> responseList = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Float>> entry : temperaturesPerDay.entrySet()) {
            List<Float> temperatures = entry.getValue();

            NetworkMeasurementResponse response = new NetworkMeasurementResponse();
            response.setTimestamp(entry.getKey().format(pattern));
            response.setMinTemp(temperatures.stream().min(Float::compare).orElse(null));
            response.setMaxTemp(temperatures.stream().max(Float::compare).orElse(null));
            response.setMedianTemp(median(temperatures));
            response.setAvgTemp((float) temperatures.stream()
                    .mapToDouble(Float::doubleValue)
                    .average()
                    .orElse(Double.NaN));
            response.setStationCount(stationsPerDay.getOrDefault(entry.getKey(), Set.of()).size());
            response.setMeasurementCount(temperatures.size());

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * Berekent de mediaan van een reeks waarden.
     *
     * <p>Bij een oneven aantal waarden is dat de middelste waarde, bij een even
     * aantal het gemiddelde van de twee middelste. Null-waarden worden
     * overgeslagen; een lege of volledig lege reeks levert {@code null} op in
     * plaats van NaN, zodat de frontend het punt kan overslaan in plaats van een
     * onzinnige waarde te tekenen.
     *
     * <p>De invoerlijst wordt niet gewijzigd.
     */
    public static Float median(Collection<Float> values) {
        if (values == null) {
            return null;
        }

        List<Float> sorted = values.stream()
                .filter(Objects::nonNull)
                .filter(value -> !value.isNaN())
                .sorted()
                .toList();

        if (sorted.isEmpty()) {
            return null;
        }

        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(middle);
        }
        return (sorted.get(middle - 1) + sorted.get(middle)) / 2f;
    }

    public static List<HourMeasurementResponse> splitIntoHourMeasurements(Collection<Measurement> measurements) {
        // TreeMap keeps the hour buckets chronological regardless of arrival
        // order, so a late-arriving reading lands in its own hour in the right
        // position rather than at the tail of the response.
        TreeMap<LocalDateTime, Set<Measurement>> hourMeasurements = new TreeMap<>();

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

    public static List<HalfHourMeasurementResponse> splitIntoHalfHourMeasurements(Collection<Measurement> measurements) {
        LinkedHashMap<LocalDateTime, Set<Measurement>> halfhourMeasurements = new LinkedHashMap<>();

        for (Measurement measurement : measurements) {
            LocalDateTime hour = LocalDateTime.ofInstant(measurement.getTimestamp(), ZoneId.systemDefault())
                    .truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime halfHour = hour.withMinute(hour.getMinute() < 30 ? 0 : 30);

            if (!halfhourMeasurements.containsKey(halfHour)) {
                halfhourMeasurements.put(halfHour, new HashSet<>());
            }
            halfhourMeasurements.get(halfHour).add(measurement);
        }

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("HH:mm");
        List<HalfHourMeasurementResponse> responseList = new ArrayList<>();

        for (Map.Entry<LocalDateTime, Set<Measurement>> entry : halfhourMeasurements.entrySet()) {
            HalfHourMeasurementResponse response = new HalfHourMeasurementResponse();
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
     * Groups measurements into time buckets (half-hour, hour or day) and returns min/max/avg temperature
     * and average pm25/pm10 per bucket.
     * Results are ordered by timestamp ascending.
     *
     * @param measurements source measurements (temperature nulls are skipped)
     * @param granularity  "half-hour", "hour" or "day" — anything else defaults to "hour"
     */
    public static List<RegionAverageBucketResponse> splitIntoRegionBuckets(Collection<Measurement> measurements, String granularity) {
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        LinkedHashMap<LocalDateTime, List<Float>> tempBuckets = new LinkedHashMap<>();
        LinkedHashMap<LocalDateTime, List<Float>> pm25Buckets = new LinkedHashMap<>();
        LinkedHashMap<LocalDateTime, List<Float>> pm10Buckets = new LinkedHashMap<>();

        measurements.stream()
                .sorted(Comparator.comparing(Measurement::getTimestamp))
                .forEach(m -> {
                    LocalDateTime bucket = toBucket(LocalDateTime.ofInstant(m.getTimestamp(), ZoneId.systemDefault()), granularity);

                    if (m.getTemperature() != null)
                        tempBuckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(m.getTemperature());
                    if (m.getPm25() != null)
                        pm25Buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(m.getPm25());
                    if (m.getPm10() != null)
                        pm10Buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(m.getPm10());
                });

        List<RegionAverageBucketResponse> result = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<Float>> entry : tempBuckets.entrySet()) {
            List<Float> temps = entry.getValue();
            List<Float> pm25s = pm25Buckets.getOrDefault(entry.getKey(), List.of());
            List<Float> pm10s = pm10Buckets.getOrDefault(entry.getKey(), List.of());

            RegionAverageBucketResponse response = new RegionAverageBucketResponse();
            response.setTimestamp(entry.getKey().format(isoFormatter));
            response.setMin(temps.stream().min(Float::compare).orElse(Float.NaN));
            response.setMax(temps.stream().max(Float::compare).orElse(Float.NaN));
            response.setAvg((float) temps.stream().mapToDouble(Float::doubleValue).average().orElse(Double.NaN));
            response.setMedian(median(temps));
            response.setPm25(pm25s.isEmpty() ? null : (float) pm25s.stream().mapToDouble(Float::doubleValue).average().orElse(Double.NaN));
            response.setPm10(pm10s.isEmpty() ? null : (float) pm10s.stream().mapToDouble(Float::doubleValue).average().orElse(Double.NaN));
            result.add(response);
        }
        return result;
    }

    /**
     * Truncates a timestamp to the start of its bucket. ChronoUnit has no half-hour unit,
     * so half-hours are rounded down to :00 or :30 manually.
     */
    private static LocalDateTime toBucket(LocalDateTime time, String granularity) {
        if ("day".equalsIgnoreCase(granularity)) {
            return time.truncatedTo(ChronoUnit.DAYS);
        }
        LocalDateTime hour = time.truncatedTo(ChronoUnit.HOURS);
        if ("half-hour".equalsIgnoreCase(granularity) && time.getMinute() >= 30) {
            return hour.plusMinutes(30);
        }
        return hour;
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