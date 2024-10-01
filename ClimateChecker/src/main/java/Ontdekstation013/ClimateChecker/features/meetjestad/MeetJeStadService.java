package Ontdekstation013.ClimateChecker.features.meetjestad;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

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

        if (!params.StationIds.isEmpty()) {
            url.append("&ids=");

            for (int stationId : params.StationIds) {
                url.append(stationId).append(",");
            }
        }

        if (params.StartDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm").withZone(ZoneOffset.UTC);
            String dateFormat = formatter.format(Instant.parse(params.StartDate.toString()));
            url.append("&begin=").append(dateFormat);
        }

        if (params.EndDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd,HH:mm").withZone(ZoneOffset.UTC);
            String dateFormat = formatter.format(Instant.parse(params.EndDate.toString()));
            url.append("&end=").append(dateFormat);
        }

        if (params.Limit != 0)
            url.append("&limit=").append(params.Limit);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url.toString(), String.class);
        String responseBody = response.getBody();

        TypeToken<List<MeasurementDto>> typeToken = new TypeToken<>() {};

        Gson gson = new Gson();
        List<MeasurementDto> measurementsDto = gson.fromJson(responseBody, typeToken);
        if (measurementsDto == null)
            measurementsDto = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC);


        List<Measurement> measurements = new ArrayList<>();
        for (MeasurementDto dto : measurementsDto) {
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

    public List<Measurement> IncorrectValueFilter(List<Measurement> measurements) {
        int differenceFromAverageDivider = 2;
        int minimumDistanceAllowed = 3;
        float total = 0;
        float min = Integer.MAX_VALUE;

        for(Measurement measurement:measurements) {
            if (measurement.getTemperature()!= null){
                total += measurement.getTemperature();
                if (measurement.getTemperature()<min){
                    min = measurement.getTemperature();
                }
            }
        }
        float adjustmentValue = Math.abs(min);
        float adjustedTotal = total + measurements.size()*adjustmentValue;
        float adjustedAverage = adjustedTotal/measurements.size();
        float allowedSpread = adjustedAverage/differenceFromAverageDivider;
        if (allowedSpread < minimumDistanceAllowed){
            allowedSpread = minimumDistanceAllowed;
        }
        float average = total/measurements.size();

        for (Measurement measurement:measurements) {
            if (measurement.getHumidity()!= null && (measurement.getHumidity()<0 || measurement.getHumidity()>100)){
                measurement.setHumidity(null);
            }
            if (measurement.getTemperature() != null){
                float absoluteDifferenceFromAverage = Math.abs(measurement.getTemperature()-average);
                if (absoluteDifferenceFromAverage > allowedSpread){
                    measurement.setTemperature(null);
                }
            }
        }
        return measurements;
    }
    }
