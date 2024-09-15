package Ontdekstation013.ClimateChecker.utility;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.time.Duration;
import java.time.Instant;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;

/**
 * We use this class for measurement-related logic that is used in both MeasurementService and NeighbourhoodService
 */
public class MeasurementLogic {

    public static List<DayMeasurementResponse> splitIntoDayMeasurements(Collection<Measurement> measurements) {
        // split measurements into days
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

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * Filters out measurements of the same station that are further away from the given timestamp
     *
     * @return closest measurement to given dateTime for each unique station in given collection
     */
    public static List<Measurement> filterClosestMeasurements(Collection<Measurement> measurements, Instant dateTime) {
        Map<Integer, Measurement> measurementHashMap = new HashMap<>();
        for (Measurement measurement : measurements) {
            int id = measurement.getId();
            if (!measurementHashMap.containsKey(id))
                measurementHashMap.put(id, measurement);
            else {
                Duration existingDifference = Duration.between(dateTime, measurementHashMap.get(id).getTimestamp()).abs();
                Duration newDifference = Duration.between(dateTime, measurement.getTimestamp()).abs();
                if (existingDifference.toSeconds() > newDifference.toSeconds())
                    measurementHashMap.put(id, measurement);
            }
        }
        return new ArrayList<>(measurementHashMap.values());
    }
}
