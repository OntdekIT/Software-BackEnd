package Ontdekstation013.ClimateChecker.features.neighbourhood;
import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodDto;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodTimelapseResponse;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.GpsTriangulation;
import Ontdekstation013.ClimateChecker.utility.MeasurementLogic;
import Ontdekstation013.ClimateChecker.utility.RegionAverageBucketResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    /** Onder- en bovengrens voor het aantal tijdstappen in een time-lapse. */
    public static final int MIN_TIMELAPSE_STEPS = 2;
    public static final int MAX_TIMELAPSE_STEPS = 48;

    /**
     * Bouwt een time-lapse van de heatmap: per tijdstap de gemiddelde temperatuur
     * en PM2.5 per wijk.
     *
     * <p>De winst zit in één enkele aanroep naar MeetJeStad. {@link #getNeighbourhoodsAtTime}
     * haalt per tijdstip het hele meetvenster op (standaard 24 uur aan metingen van
     * alle stations); dat 24 keer achter elkaar doen voor een time-lapse is zonde,
     * want die vensters overlappen bijna volledig. Hier wordt het bereik
     * {@code [from - venster, to]} één keer opgehaald en worden alle stappen uit
     * diezelfde verzameling berekend.
     *
     * <p>Tweede optimalisatie: elke meting ligt op een vast punt, dus in welke wijk
     * een meting valt hoeft maar één keer bepaald te worden in plaats van per
     * tijdstap opnieuw. Het dure point-in-polygon werk gebeurt daarmee N keer in
     * plaats van N × aantal stappen keer.
     *
     * @param from  tijdstip van de eerste stap
     * @param to    tijdstip van de laatste stap
     * @param steps aantal stappen, inclusief begin en eind
     */
    public NeighbourhoodTimelapseResponse getNeighbourhoodTimelapse(Instant from, Instant to, int steps) {
        if (steps < MIN_TIMELAPSE_STEPS || steps > MAX_TIMELAPSE_STEPS) {
            throw new InvalidArgumentException(
                    "steps must be between " + MIN_TIMELAPSE_STEPS + " and " + MAX_TIMELAPSE_STEPS);
        }
        if (!from.isBefore(to)) {
            throw new InvalidArgumentException("'from' must be before 'to'");
        }

        List<Neighbourhood> neighbourhoods = neighbourhoodRepository.findAll();
        int minuteMargin = meetJeStadService.getMinuteLimit();

        // Eén aanroep voor het hele bereik, inclusief het meetvenster vóór de
        // eerste stap zodat ook die stap metingen kan vinden.
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = from.minus(Duration.ofMinutes(minuteMargin));
        params.EndDate = to;
        params.includeFaultyMeasurements = false;
        List<Measurement> allMeasurements = meetJeStadService.getMeasurements(params);

        // Wijkgrenzen en de wijk per meting eenmalig bepalen.
        List<NeighbourhoodTimelapseResponse.TimelapseNeighbourhood> neighbourhoodDtos = new ArrayList<>();
        Map<Long, float[][]> polygonsById = new LinkedHashMap<>();
        for (Neighbourhood neighbourhood : neighbourhoods) {
            float[][] coordinates = convertToFloatArray(neighbourhood.getCoordinates());
            polygonsById.put(neighbourhood.getId(), coordinates);
            neighbourhoodDtos.add(new NeighbourhoodTimelapseResponse.TimelapseNeighbourhood(
                    neighbourhood.getId(), neighbourhood.getName(), coordinates));
        }

        Map<Measurement, Long> neighbourhoodByMeasurement = new IdentityHashMap<>();
        for (Measurement measurement : allMeasurements) {
            float[] point = { measurement.getLatitude(), measurement.getLongitude() };
            for (Map.Entry<Long, float[][]> entry : polygonsById.entrySet()) {
                if (GpsTriangulation.pointInPolygon(entry.getValue(), point)) {
                    neighbourhoodByMeasurement.put(measurement, entry.getKey());
                    break;
                }
            }
        }

        // Metingen op tijd sorteren zodat het venster per stap met een simpele
        // filter te bepalen is.
        List<Measurement> sorted = allMeasurements.stream()
                .sorted(Comparator.comparing(Measurement::getTimestamp))
                .toList();

        List<NeighbourhoodTimelapseResponse.TimelapseFrame> frames = new ArrayList<>();
        long totalSeconds = Duration.between(from, to).getSeconds();

        for (int step = 0; step < steps; step++) {
            // Afstand vanaf 'from' naar rato van de stap; de laatste stap valt
            // precies op 'to'.
            Instant stepTime = from.plusSeconds(Math.round((double) totalSeconds * step / (steps - 1)));
            Instant windowStart = stepTime.minus(Duration.ofMinutes(minuteMargin));

            List<Measurement> inWindow = sorted.stream()
                    .filter(m -> !m.getTimestamp().isAfter(stepTime) && !m.getTimestamp().isBefore(windowStart))
                    .toList();

            List<Measurement> closest = MeasurementLogic.filterClosestMeasurements(inWindow, stepTime);

            Map<Long, List<Measurement>> perNeighbourhood = new HashMap<>();
            for (Measurement measurement : closest) {
                Long neighbourhoodId = neighbourhoodByMeasurement.get(measurement);
                if (neighbourhoodId != null) {
                    perNeighbourhood.computeIfAbsent(neighbourhoodId, k -> new ArrayList<>()).add(measurement);
                }
            }

            List<NeighbourhoodTimelapseResponse.TimelapseValue> values = new ArrayList<>();
            for (Long neighbourhoodId : polygonsById.keySet()) {
                List<Measurement> measurementsHere = perNeighbourhood.getOrDefault(neighbourhoodId, List.of());

                OptionalDouble avgTemp = measurementsHere.stream()
                        .map(Measurement::getTemperature)
                        .filter(Objects::nonNull)
                        .mapToDouble(Float::doubleValue)
                        .average();
                OptionalDouble avgPm25 = measurementsHere.stream()
                        .map(Measurement::getPm25)
                        .filter(Objects::nonNull)
                        .mapToDouble(Float::doubleValue)
                        .average();

                // Null in plaats van NaN: de frontend slaat een wijk zonder meting
                // dan over in plaats van hem als 0 te kleuren.
                values.add(new NeighbourhoodTimelapseResponse.TimelapseValue(
                        neighbourhoodId,
                        avgTemp.isPresent() ? (float) avgTemp.getAsDouble() : null,
                        avgPm25.isPresent() ? (float) avgPm25.getAsDouble() : null));
            }

            frames.add(new NeighbourhoodTimelapseResponse.TimelapseFrame(
                    stepTime.toString(), values, closest.size()));
        }

        return new NeighbourhoodTimelapseResponse(neighbourhoodDtos, frames);
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
