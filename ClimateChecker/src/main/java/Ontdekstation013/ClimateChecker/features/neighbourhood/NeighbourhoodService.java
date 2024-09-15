package Ontdekstation013.ClimateChecker.features.neighbourhood;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodDTO;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import Ontdekstation013.ClimateChecker.utility.MeasurementLogic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NeighbourhoodService {
    // Latitude = Y
    // Longitude = X

    private final MeetJeStadService meetJeStadService;
    private final NeighbourhoodRepository neighbourhoodRepository;

    // Process measurements into neighbourhoods
    private List<NeighbourhoodDTO> getNeighbourhoodsAverageTemp(List<Neighbourhood> neighbourhoods, List<Measurement> measurements){
        List<NeighbourhoodDTO> neighbourhoodDTOS = new ArrayList<>();

        for (Neighbourhood neighbourhood : neighbourhoods) {
            NeighbourhoodDTO dto = new NeighbourhoodDTO();

            dto.setId(neighbourhood.getId());
            dto.setName(neighbourhood.getName());
            dto.setCoordinates(convertToFloatArray(neighbourhood.coordinates));

            // Get all measurements within this neighbourhood
            List<Measurement> tempMeasurements = new ArrayList<>();
            for (Measurement measurement : measurements) {
                float[] point = { measurement.getLatitude(), measurement.getLongitude() };
                if (GpsTriangulation.pointInPolygon(dto.getCoordinates(), point)) {
                    tempMeasurements.add(measurement);
                }
            }

            // Calculate average temperature of the measurements in this neighbourhood
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

            neighbourhoodDTOS.add(dto);
        }
        return neighbourhoodDTOS;
    }

    public List<NeighbourhoodDTO> getNeighbourhoodsAtTime(Instant dateTime){
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

        // This code is a patch to get the data from all stations in a neighbourhood, it can be improved with data caching
        //      First get all measurements from a 1-hour period to get "all" operational stationIds,
        //      the longer timespan you query, the more stations will be included, but the longer it will take
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = endDate.minusSeconds(60 * 60);
        params.EndDate = endDate;
        params.includeFaultyMeasurements = false;
        List<Measurement> measurements = meetJeStadService.getMeasurements(params);
        //      Then get all station id's within this neighbourhood
        float[][] neighbourhoodCoords = convertToFloatArray(neighbourhood.coordinates);
        List<Integer> stations = new ArrayList<>();
        for (Measurement measurement : measurements) {
            float[] point = { measurement.getLatitude(), measurement.getLongitude() };
            if (GpsTriangulation.pointInPolygon(neighbourhoodCoords, point) && !stations.contains(measurement.getId()))
                stations.add(measurement.getId());
        }
        //      If no stations in neighbourhood, no query has to be done
        //      Removing this code causes MeetJeStadService to get the data of ALL stations (+/- 350.000 for 1 month)
        if (stations.isEmpty())
            return new ArrayList<DayMeasurementResponse>();
        //      Lastly, get all measurements within timeframe from these stations
        params = new MeetJeStadParameters();
        params.StartDate = startDate;
        params.EndDate = endDate;
        params.StationIds = stations;
        params.includeFaultyMeasurements = true;
        measurements = meetJeStadService.getMeasurements(params);

        return MeasurementLogic.splitIntoDayMeasurements(measurements);
    }

    /**
     * Converts list of {@link NeighbourhoodCoords} into format required for {@link NeighbourhoodDTO} and {@link GpsTriangulation#pointInPolygon(float[][], float[]) pointInPolygon}
     */
    private float[][] convertToFloatArray(List<NeighbourhoodCoords> coordinates) {
        return coordinates.stream()
                .map(coord -> new float[]{ coord.getLatitude(), coord.getLongitude() })
                .toArray(float[][]::new);
    }
}