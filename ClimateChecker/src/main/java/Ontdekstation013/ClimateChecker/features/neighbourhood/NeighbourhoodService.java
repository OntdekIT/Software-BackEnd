package Ontdekstation013.ClimateChecker.features.neighbourhood;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodDto;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import Ontdekstation013.ClimateChecker.utility.MeasurementLogic;
import Ontdekstation013.ClimateChecker.utility.RegionAverageBucketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NeighbourhoodService {
    private final MeetJeStadService meetJeStadService;
    private final NeighbourhoodRepository neighbourhoodRepository;

    private List<NeighbourhoodDto> getNeighbourhoodsAverageTemp(List<Neighbourhood> neighbourhoods, List<Measurement> measurements) {
        List<NeighbourhoodDto> neighbourhoodDtos = new ArrayList<>();

        for (Neighbourhood neighbourhood : neighbourhoods) {
            NeighbourhoodDto dto = new NeighbourhoodDto();

            dto.setId(neighbourhood.getId());
            dto.setName(neighbourhood.getName());
            float[][] coordinates = convertToFloatArray(neighbourhood.getCoordinates());
            dto.setCoordinates(coordinates);

            List<Measurement> measurementsInNeighbourhood = filterMeasurementsInPolygon(measurements, coordinates);

            OptionalDouble avgTemp = measurementsInNeighbourhood.stream()
                    .map(Measurement::getTemperature)
                    .filter(Objects::nonNull)
                    .mapToDouble(Float::doubleValue)
                    .average();

            dto.setAvgTemp(avgTemp.isPresent() ? (float) avgTemp.getAsDouble() : Float.NaN);

            OptionalDouble avgPm25 = measurementsInNeighbourhood.stream()
                    .map(Measurement::getPm25)
                    .filter(Objects::nonNull)
                    .mapToDouble(Float::doubleValue)
                    .average();

            dto.setAvgPm25(avgPm25.isPresent() ? (float) avgPm25.getAsDouble() : null);

            neighbourhoodDtos.add(dto);
        }
        return neighbourhoodDtos;
    }

    public List<NeighbourhoodDto> getNeighbourhoodsAtTime(Instant dateTime) {
        List<Neighbourhood> neighbourhoods = neighbourhoodRepository.findAll();

        int minuteMargin = meetJeStadService.getMinuteLimit();
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = dateTime.minus(Duration.ofMinutes(minuteMargin));
        params.EndDate = dateTime;
        params.includeFaultyMeasurements = false;
        List<Measurement> allMeasurements = meetJeStadService.getMeasurements(params);

        List<Measurement> closestMeasurements = MeasurementLogic.filterClosestMeasurements(allMeasurements, dateTime);

        return getNeighbourhoodsAverageTemp(neighbourhoods, closestMeasurements);
    }

    public List<DayMeasurementResponse> getHistoricalNeighbourhoodData(Long id, Instant startDate, Instant endDate) {
        Optional<Neighbourhood> neighbourhoodOptional = neighbourhoodRepository.findById(id);
        if (neighbourhoodOptional.isEmpty()) {
            return new ArrayList<>();
        }

        Neighbourhood neighbourhood = neighbourhoodOptional.get();
        float[][] neighbourhoodCoords = convertToFloatArray(neighbourhood.getCoordinates());

        MeetJeStadParameters historicalParams = new MeetJeStadParameters();
        historicalParams.StartDate = startDate;
        historicalParams.EndDate = endDate;
        historicalParams.includeFaultyMeasurements = true;
        List<Measurement> allMeasurements = meetJeStadService.getMeasurements(historicalParams);

        Set<Integer> stationIds = allMeasurements.stream()
                .filter(measurement -> {
                    if (measurement.getStation() == null) {
                        return false;
                    }
                    float[] point = { measurement.getLatitude(), measurement.getLongitude() };
                    return GpsTriangulation.pointInPolygon(neighbourhoodCoords, point);
                })
                .map(measurement -> Math.toIntExact(measurement.getStation().getStationid()))
                .collect(Collectors.toSet());

        if (stationIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Measurement> neighbourhoodMeasurements = allMeasurements.stream()
                .filter(measurement -> measurement.getStation() != null &&
                        stationIds.contains(Math.toIntExact(measurement.getStation().getStationid())))
                .collect(Collectors.toList());

        return MeasurementLogic.splitIntoDayMeasurements(neighbourhoodMeasurements);
    }

    public List<RegionAverageBucketResponse> getRegionAverageHistory(Long regionId, Instant from, Instant to, String granularity) {
        Neighbourhood neighbourhood = neighbourhoodRepository.findById(regionId)
                .orElseThrow(() -> new NotFoundException("Region not found: " + regionId));

        float[][] neighbourhoodCoords = convertToFloatArray(neighbourhood.getCoordinates());

        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = from;
        params.EndDate = to;
        params.includeFaultyMeasurements = true;
        List<Measurement> allMeasurements = meetJeStadService.getMeasurements(params);

        Set<Integer> stationIds = allMeasurements.stream()
                .filter(m -> m.getStation() != null)
                .filter(m -> {
                    float[] point = { m.getLatitude(), m.getLongitude() };
                    return GpsTriangulation.pointInPolygon(neighbourhoodCoords, point);
                })
                .map(m -> Math.toIntExact(m.getStation().getStationid()))
                .collect(Collectors.toSet());

        if (stationIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Measurement> regionMeasurements = allMeasurements.stream()
                .filter(m -> m.getStation() != null &&
                        stationIds.contains(Math.toIntExact(m.getStation().getStationid())))
                .collect(Collectors.toList());

        return MeasurementLogic.splitIntoRegionBuckets(regionMeasurements, granularity);
    }

    private List<Measurement> filterMeasurementsInPolygon(List<Measurement> measurements, float[][] polygon) {
        return measurements.stream()
                .filter(measurement -> {
                    float[] point = { measurement.getLatitude(), measurement.getLongitude() };
                    return GpsTriangulation.pointInPolygon(polygon, point);
                })
                .collect(Collectors.toList());
    }

    private float[][] convertToFloatArray(List<NeighbourhoodCoords> coordinates) {
        return coordinates.stream()
                .map(coord -> new float[]{ coord.getLatitude(), coord.getLongitude() })
                .toArray(float[][]::new);
    }

    public NeighbourhoodDto getNeighbourhoodById(Long id)
    {
        Neighbourhood neighbourhood = neighbourhoodRepository.findById(id).orElseThrow(() -> new NotFoundException("Neighbourhood not found"));

        NeighbourhoodDto dto = new NeighbourhoodDto();
        dto.setId(neighbourhood.getId());
        dto.setName(neighbourhood.getName());
        dto.setCoordinates(convertToFloatArray(neighbourhood.getCoordinates()));
        dto.setAvgTemp(Float.NaN);

        return dto;
    }


}
