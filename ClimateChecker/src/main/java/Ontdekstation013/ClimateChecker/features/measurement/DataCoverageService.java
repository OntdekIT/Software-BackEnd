package Ontdekstation013.ClimateChecker.features.measurement;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.utility.DataCoverageLogic;
import Ontdekstation013.ClimateChecker.utility.DataCoverageResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Shows where and when particulate matter data is missing, so the reliability of
 * the measuring network can be judged.
 */
@Service
@RequiredArgsConstructor
public class DataCoverageService {
    // Every request pulls the raw readings of all stations from MeetJeStad, so
    // the period is capped to keep the response time and payload bounded.
    public static final Duration MAX_RANGE = Duration.ofDays(31);
    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");

    private final MeetJeStadService meetJeStadService;
    private final StationRepository stationRepository;

    public DataCoverageResponse getPmCoverage(Instant from, Instant to, String granularity) {
        DataCoverageLogic.Granularity parsedGranularity = DataCoverageLogic.Granularity.parse(granularity);
        if (!from.isBefore(to)) {
            throw new InvalidArgumentException("'from' must be before 'to'");
        }
        if (Duration.between(from, to).compareTo(MAX_RANGE) > 0) {
            throw new InvalidArgumentException("The period can be at most " + MAX_RANGE.toDays() + " days");
        }

        List<Station> stations = stationRepository.findAll();

        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = DataCoverageLogic.alignToBucket(from, parsedGranularity, ZONE);
        params.EndDate = to;
        // Faulty values still prove the station sent something; we are measuring
        // availability here, not quality.
        params.includeFaultyMeasurements = true;
        // The station sync stores every station inside the city limits, so asking
        // only for those ids keeps the payload small. With an empty database we
        // fall back to all stations.
        for (Station station : stations) {
            params.StationIds.add(station.getStationid().intValue());
        }

        List<Measurement> measurements = meetJeStadService.getMeasurements(params);
        return DataCoverageLogic.calculate(measurements, stations, from, to, parsedGranularity, ZONE);
    }
}
