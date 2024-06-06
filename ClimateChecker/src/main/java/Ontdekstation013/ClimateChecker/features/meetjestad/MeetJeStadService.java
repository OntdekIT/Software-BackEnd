package Ontdekstation013.ClimateChecker.features.meetjestad;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDTO;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

@Service
public class MeetJeStadService {
    private final String baseUrl = "https://meetjestad.net/data/?type=sensors&format=json";
    @Getter
    private final int minuteLimit = 35;
    private final float[][] cityLimits = {
            {51.65156373065635f, 5.217787919413907f},
            {51.51818572766462f, 5.227145728754213f},
            {51.52666590649518f, 4.911805911309284f},
            {51.65077670571181f, 4.957086656750303f}
    };

    public List<Measurement> getMeasurements(MeetJeStadParameters params) {
        StringBuilder url = new StringBuilder(baseUrl);

        // Do we want a specific set of stations or all stations
        if (!params.StationIds.isEmpty()) {
            url.append("&ids=");

            for (int stationId : params.StationIds) {
                url.append(stationId).append(",");
            }
        }

        // Get measurements from this date
        if (params.StartDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm").withZone(ZoneOffset.UTC);
            String dateFormat = formatter.format(Instant.parse(params.StartDate.toString()));
            url.append("&begin=").append(dateFormat);
        }

        // Get measurements until this date
        if (params.EndDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm").withZone(ZoneOffset.UTC);
            String dateFormat = formatter.format(Instant.parse(params.EndDate.toString()));
            url.append("&end=").append(dateFormat);
        }

        // Do we limit the amount of measurements
        if (params.Limit != 0)
            url.append("&limit=").append(params.Limit);



        // Execute call and convert to json
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url.toString(), String.class);
        String responseBody = response.getBody();

        // Convert json to list object
        TypeToken<List<MeasurementDTO>> typeToken = new TypeToken<>() {};

        Gson gson = new Gson();
        List<MeasurementDTO> measurementsDto = gson.fromJson(responseBody, typeToken);
        // Set as empty array if null
        if (measurementsDto == null)
            measurementsDto = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")   // input pattern
                .withZone(ZoneOffset.UTC);          // input timezone


        List<Measurement> measurements = new ArrayList<>();
        // Convert MeasurementDTO to Measurement
        for (MeasurementDTO dto : measurementsDto) {
            // Filter out measurements which are outside city bounds
            float[] point = {dto.getLatitude(), dto.getLongitude()};
            if (!GpsTriangulation.pointInPolygon(cityLimits, point))
                continue;

            Measurement measurement = new Measurement();
            measurement.setId(dto.getId());
            measurement.setLongitude(dto.getLongitude());
            measurement.setLatitude(dto.getLatitude());
            measurement.setTemperature(dto.getTemperature());
            measurement.setHumidity(dto.getHumidity());

            TemporalAccessor temp = formatter.parse(dto.getTimestamp());
            measurement.setTimestamp(Instant.from(temp));

            measurements.add(measurement);
        }

        if (params.includeFaultyMeasurements)
            return measurements;
        else
            return IncorrectValueFilter(measurements);
    }

    public static List<Measurement> IncorrectValueFilter(List<Measurement> measurements) {
        List<Measurement> filteredMeasurements = new ArrayList<>();

        // Sort measurements by time
        Collections.sort(measurements, Comparator.comparingLong(m -> m.getTimestamp().toEpochMilli()));

        for (int i = 0; i < measurements.size(); i++) {
            Measurement current = measurements.get(i);

            // Check the first 2 measurements before and after the current measurement
            int start = Math.max(0, i - 2);
            int end = Math.min(measurements.size() - 1, i + 2);

            double valueThreshold = current.getTemperature() * 0.20; // 10% threshold for value
            double humidityThreshold = current.getHumidity() * 0.20; // 20% threshold for humidity

            int countOverThreshold = 0;

            for (int j = start; j <= end; j++) {
                if (j != i) {
                    Measurement other = measurements.get(j);
                    if (Math.abs(current.getTemperature() - other.getTemperature()) > valueThreshold ||
                            Math.abs(current.getHumidity() - other.getHumidity()) > humidityThreshold) {
                        countOverThreshold++;
                        if (countOverThreshold >= 2) {
                            break; // If 2 or more are over threshold, no need to check further
                        }
                    }
                }
            }

            // If at least 2 comparisons are over the threshold, don't add the measurement
            if (countOverThreshold < 3) {
                filteredMeasurements.add(current);
            }
        }

        return filteredMeasurements;
    }
    }
