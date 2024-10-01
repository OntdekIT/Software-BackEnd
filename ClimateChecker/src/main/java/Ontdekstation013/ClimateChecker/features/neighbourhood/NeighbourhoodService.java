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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NeighbourhoodService {
    // Latitude = Y
    // Longitude = X

    private final MeetJeStadService meetJeStadService;
    private final INeighbourhoodRepository neighbourhoodRepository;

    private List<NeighbourhoodDto> getNeighbourhoodsAverageTemp(List<Neighbourhood> neighbourhoods, List<Measurement> measurements){
        List<NeighbourhoodDto> neighbourhoodDtos = new ArrayList<>();

        for (Neighbourhood neighbourhood : neighbourhoods) {
            NeighbourhoodDto dto = new NeighbourhoodDto();

            dto.setId(neighbourhood.getId());
            dto.setName(neighbourhood.getName());
            dto.setCoordinates(convertToFloatArray(neighbourhood.coordinates));

            List<Measurement> tempMeasurements = new ArrayList<>();
            for (Measurement measurement : measurements) {
                float[] point = { measurement.getLatitude(), measurement.getLongitude() };
                if (GpsTriangulation.pointInPolygon(dto.getCoordinates(), point)) {
                    tempMeasurements.add(measurement);
                }
            }

            float totalTemp = 0.0f;
            int measurementCount = tempMeasurements.size();
            for (Measurement measurement : tempMeasurements) {
                if (measurement.getTemperature() != null) {
                    totalTemp += measurement.getTemperature();
                }
                else {
                    measurementCount--;
                }
            }
            dto.setAvgTemp(totalTemp / measurementCount);

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
        Neighbourhood neighbourhood;
        if (neighbourhoodOptional.isPresent())
              neighbourhood = neighbourhoodOptional.get();
        else
            return new ArrayList<>();

        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = endDate.minusSeconds(60 * 60);
        params.EndDate = endDate;
        params.includeFaultyMeasurements = false;
        List<Measurement> measurements = meetJeStadService.getMeasurements(params);

        float[][] neighbourhoodCoords = convertToFloatArray(neighbourhood.coordinates);
        List<Integer> stations = new ArrayList<>();
        for (Measurement measurement : measurements) {
            float[] point = { measurement.getLatitude(), measurement.getLongitude() };
            if (GpsTriangulation.pointInPolygon(neighbourhoodCoords, point) && !stations.contains(measurement.getId()))
                stations.add(measurement.getId());
        }

        if (stations.isEmpty())
            return new ArrayList<DayMeasurementResponse>();

        params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds = stations;
        params.includeFaultyMeasurements = true;
        measurements = meetJeStadService.getMeasurements(params);

        return MeasurementLogic.splitIntoDayMeasurements(measurements);
    }

    /**
     * Converts list of {@link NeighbourhoodCoords} into format required for {@link NeighbourhoodDto} and {@link GpsTriangulation#pointInPolygon(float[][], float[]) pointInPolygon}
     */
    private float[][] convertToFloatArray(List<NeighbourhoodCoords> coordinates) {
        return coordinates.stream()
                .map(coord -> new float[]{ coord.getLatitude(), coord.getLongitude() })
                .toArray(float[][]::new);
    }
}