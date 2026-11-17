package Ontdekstation013.ClimateChecker.features.meetjestad;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import Ontdekstation013.ClimateChecker.utility.MeasurementDeduplicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MeetJeStadService {
    // Databron-URL komt uit de configuratie (property meetjestad.base-url /
    // env MEETJESTAD_BASE_URL) zodat je zonder codewijziging naar een andere
    // bron (demo/test/live) kunt wijzen. De default is de live-bron.
    private final String baseUrl;
    @Getter
    private final int minuteLimit = 1440;
    private final float[][] cityLimits = {
            {51.65156373065635f, 5.217787919413907f},
            {51.51818572766462f, 5.227145728754213f},
            {51.52666590649518f, 4.911805911309284f},
            {51.65077670571181f, 4.957086656750303f}
    };
    private final StationRepository stationRepository;
    private final RestTemplate restTemplate;
    // Plausibele grenzen voor meetwaarden, instelbaar via application.properties
    // (met env-override). Waarden erbuiten worden op null gezet.
    private final float humidityMin;
    private final float humidityMax;
    private final float pm25Min;
    private final float pm25Max;
    private final float pm10Min;
    private final float pm10Max;

    public MeetJeStadService(
            StationRepository stationRepository,
            @Value("${meetjestad.base-url:https://meetjestad.net/data/?type=sensors&format=json}") String baseUrl,
            @Value("${measurement.limits.humidity-min:0}") float humidityMin,
            @Value("${measurement.limits.humidity-max:100}") float humidityMax,
            @Value("${measurement.limits.pm25-min:0}") float pm25Min,
            @Value("${measurement.limits.pm25-max:1000}") float pm25Max,
            @Value("${measurement.limits.pm10-min:0}") float pm10Min,
            @Value("${measurement.limits.pm10-max:1000}") float pm10Max) {
        this.stationRepository = stationRepository;
        this.baseUrl = baseUrl;
        this.humidityMin = humidityMin;
        this.humidityMax = humidityMax;
        this.pm25Min = pm25Min;
        this.pm25Max = pm25Max;
        this.pm10Min = pm10Min;
        this.pm10Max = pm10Max;

        // Bounded timeouts so a slow or hanging meetjestad.net cannot tie up
        // request threads indefinitely.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        this.restTemplate = new RestTemplate(requestFactory);
    }

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

        Map<Integer, float[]> latestValidLocations = findLatestValidLocations(measurementsDto);
        Map<Long, Station> locationUpdates = new HashMap<>();

        List<Measurement> measurements = new ArrayList<>();
        for (MeasurementDto dto : measurementsDto) {
            Station station = stationRepository.getMeetstationByStationid((long) dto.getId());
            boolean knownStation = station != null;

            if (station == null) {
                station = new Station();
                station.setStationid((long) dto.getId());
                station.setName("Unknown Station " + dto.getId());
                station.setIsActive(false);
                station.setIs_public(false);
                station.setRegistrationCode(0L);
                station.setMeasurementList(new ArrayList<>());
            }

            float[] point;
            boolean inherited = false;
            if (hasValidLocation(dto)) {
                point = new float[]{dto.getLatitude(), dto.getLongitude()};
                // Een echte positie buiten Tilburg hoort niet op onze kaart.
                if (!GpsTriangulation.pointInPolygon(cityLimits, point))
                    continue;
            } else {
                point = lastKnownLocation(station, latestValidLocations.get(dto.getId()));
                // Zonder enige bekende positie kunnen we de meting niet plaatsen.
                if (point == null || !GpsTriangulation.pointInPolygon(cityLimits, point))
                    continue;
                inherited = true;
            }
            dto.setLatitude(point[0]);
            dto.setLongitude(point[1]);

            Measurement measurement = new Measurement();
            measurement.setStation(station);
            measurement.setLocationInherited(inherited);
            measurement.setLongitude(point[1]);
            measurement.setLatitude(point[0]);
            measurement.setTemperature(dto.getTemperature());
            measurement.setHumidity(dto.getHumidity());
            measurement.setPm25(dto.getPm25());
            measurement.setPm10(dto.getPm10());

            TemporalAccessor temp = formatter.parse(dto.getTimestamp());
            measurement.setTimestamp(Instant.from(temp));

            measurements.add(measurement);

            if (knownStation && !inherited) {
                rememberLocation(station, point, latestValidLocations.get(dto.getId()), locationUpdates);
            }
        }
        locationUpdates.values().forEach(stationRepository::save);

        // Collapse duplicate/resent rows for the same (station, timestamp)
        // before anything downstream aggregates them, otherwise a resend is
        // counted twice in every average/min/max.
        measurements = MeasurementDeduplicator.deduplicate(measurements);

        if (params.includeFaultyMeasurements)
            return measurements;
        else
            return IncorrectValueFilter(measurements);
    }

    // Een positie is geldig als hij echt is doorgegeven: niet ontbrekend, niet NaN
    // en niet het "0,0"-punt dat een station zonder GPS-ontvangst oplevert.
    static boolean hasValidLocation(MeasurementDto dto) {
        Float lat = dto.getLatitude();
        Float lon = dto.getLongitude();
        if (lat == null || lon == null || lat.isNaN() || lon.isNaN() || lat.isInfinite() || lon.isInfinite())
            return false;
        return !(lat == 0f && lon == 0f);
    }

    // Nieuwste geldige positie per station binnen deze batch (op tijdstip).
    private Map<Integer, float[]> findLatestValidLocations(List<MeasurementDto> dtos) {
        Map<Integer, String> latestTimestamp = new HashMap<>();
        Map<Integer, float[]> latest = new HashMap<>();
        for (MeasurementDto dto : dtos) {
            if (!hasValidLocation(dto) || dto.getTimestamp() == null)
                continue;
            float[] point = {dto.getLatitude(), dto.getLongitude()};
            if (!GpsTriangulation.pointInPolygon(cityLimits, point))
                continue;
            String previous = latestTimestamp.get(dto.getId());
            if (previous == null || dto.getTimestamp().compareTo(previous) > 0) {
                latestTimestamp.put(dto.getId(), dto.getTimestamp());
                latest.put(dto.getId(), point);
            }
        }
        return latest;
    }

    private float[] lastKnownLocation(Station station, float[] latestInBatch) {
        if (latestInBatch != null)
            return latestInBatch;
        if (station.getLastKnownLatitude() != null && station.getLastKnownLongitude() != null)
            return new float[]{station.getLastKnownLatitude(), station.getLastKnownLongitude()};
        return null;
    }

    // Bewaar de nieuwste geldige positie op het station, alleen als die veranderd is.
    private void rememberLocation(Station station, float[] point, float[] latestInBatch, Map<Long, Station> updates) {
        if (latestInBatch == null || point[0] != latestInBatch[0] || point[1] != latestInBatch[1])
            return;
        Float storedLat = station.getLastKnownLatitude();
        Float storedLon = station.getLastKnownLongitude();
        if (storedLat != null && storedLon != null && storedLat == point[0] && storedLon == point[1])
            return;
        station.setLastKnownLatitude(point[0]);
        station.setLastKnownLongitude(point[1]);
        updates.put(station.getStationid(), station);
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
            if (isOutOfRange(measurement.getHumidity(), humidityMin, humidityMax)) {
                measurement.setHumidity(null);
            }
            if (isOutOfRange(measurement.getPm25(), pm25Min, pm25Max)) {
                measurement.setPm25(null);
            }
            if (isOutOfRange(measurement.getPm10(), pm10Min, pm10Max)) {
                measurement.setPm10(null);
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

    private static boolean isOutOfRange(Float value, float min, float max) {
        return value != null && (value.isNaN() || value < min || value > max);
    }
}
