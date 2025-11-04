package Ontdekstation013.ClimateChecker.features.neighbourhood;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodDto;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import Ontdekstation013.ClimateChecker.utility.MeasurementLogic;
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

    private List<NeighbourhoodDto> getNeighbourhoodsAverageTemp(List<Neighbourhood> neighbourhoods, List<Measurement> measurements){
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
            neighbourhoodDtos.add(dto);
        }
        return neighbourhoodDtos;
    }

    public List<NeighbourhoodDto> getNeighbourhoodsAtTime(Instant dateTime){
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
}
