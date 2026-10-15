package Ontdekstation013.ClimateChecker.features.measurement;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.utility.DataCoverageLogic;
import Ontdekstation013.ClimateChecker.utility.DataCoverageResponse;
import Ontdekstation013.ClimateChecker.utility.MeasurementDeduplicator;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    // MeetJeStad calls have a 30s read timeout; fetching the period in chunks
    // keeps each call well below that, even for a full month of raw data.
    static final Duration FETCH_CHUNK = Duration.ofDays(7);
    // The frontend asks for whole hours, so the same period is requested again
    // and again while an admin switches filters. A short cache spares MeetJeStad.
    static final Duration CACHE_TTL = Duration.ofMinutes(15);
    static final int CACHE_MAX_ENTRIES = 16;
    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");

    private final MeetJeStadService meetJeStadService;
    private final StationRepository stationRepository;
    private final Map<CacheKey, CachedResponse> cache = new ConcurrentHashMap<>();

    public DataCoverageResponse getPmCoverage(Instant from, Instant to, String granularity) {
        DataCoverageLogic.Granularity parsedGranularity = DataCoverageLogic.Granularity.parse(granularity);
        if (!from.isBefore(to)) {
            throw new InvalidArgumentException("'from' must be before 'to'");
        }
        Instant start = DataCoverageLogic.alignToBucket(from, parsedGranularity, ZONE);
        if (Duration.between(start, to).compareTo(MAX_RANGE) > 0) {
            throw new InvalidArgumentException("The period can be at most " + MAX_RANGE.toDays() + " days");
        }

        CacheKey key = new CacheKey(start, to, parsedGranularity);
        Instant now = Instant.now();
        CachedResponse cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return cached.response();
        }

        DataCoverageResponse response = calculate(from, to, start, parsedGranularity);
        cache.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        if (cache.size() < CACHE_MAX_ENTRIES) {
            cache.put(key, new CachedResponse(response, now.plus(CACHE_TTL)));
        }
        return response;
    }

    private DataCoverageResponse calculate(Instant from, Instant to, Instant start,
                                           DataCoverageLogic.Granularity granularity) {
        List<Station> stations = stationRepository.findAll();
        // The station sync stores every station inside the city limits. Without
        // any stations there is nothing to report, and an empty id list would make
        // MeetJeStad return the data of every station it knows.
        if (stations.isEmpty()) {
            return DataCoverageLogic.calculate(List.of(), stations, from, to, granularity, ZONE);
        }

        List<Integer> stationIds = new ArrayList<>();
        for (Station station : stations) {
            stationIds.add(station.getStationid().intValue());
        }

        List<Measurement> measurements = new ArrayList<>();
        for (Instant chunkStart = start; chunkStart.isBefore(to); chunkStart = chunkStart.plus(FETCH_CHUNK)) {
            Instant chunkEnd = chunkStart.plus(FETCH_CHUNK).isBefore(to) ? chunkStart.plus(FETCH_CHUNK) : to;
            measurements.addAll(meetJeStadService.getMeasurements(chunkParams(stationIds, chunkStart, chunkEnd)));
        }
        // A reading exactly on a chunk boundary can come back in both chunks.
        measurements = MeasurementDeduplicator.deduplicate(measurements);
        return DataCoverageLogic.calculate(measurements, stations, from, to, granularity, ZONE);
    }

    private static MeetJeStadParameters chunkParams(List<Integer> stationIds, Instant from, Instant to) {
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.StartDate = from;
        params.EndDate = to;
        // Faulty values still prove the station sent something; we are measuring
        // availability here, not quality.
        params.includeFaultyMeasurements = true;
        params.StationIds.addAll(stationIds);
        return params;
    }

    private record CacheKey(Instant from, Instant to, DataCoverageLogic.Granularity granularity) {
    }

    private record CachedResponse(DataCoverageResponse response, Instant expiresAt) {
    }
}
