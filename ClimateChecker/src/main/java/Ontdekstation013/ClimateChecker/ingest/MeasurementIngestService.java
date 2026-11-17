package Ontdekstation013.ClimateChecker.ingest;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bepaalt het incrementele venster en slaat opgehaalde metingen idempotent op. */
@Service
@RequiredArgsConstructor
public class MeasurementIngestService {
    private final MeasurementRepository measurementRepository;
    private final StationRepository stationRepository;
    private final IngestProperties properties;

    /**
     * Begin van het op te halen venster. Na een eerdere run: einde van die run min de overlap.
     * Anders: de nieuwste meting in de database min de overlap (begrensd op de maximale
     * inhaalperiode), of de beginperiode als de database leeg is.
     */
    public Instant windowStart(Instant now, Instant lastFetchedUntil) {
        Duration overlap = Duration.ofMinutes(properties.getOverlapMinutes());
        if (lastFetchedUntil != null) {
            return lastFetchedUntil.minus(overlap);
        }
        Instant latest = measurementRepository.findLatestTimestamp();
        if (latest == null) {
            return now.minus(Duration.ofHours(properties.getInitialWindowHours()));
        }
        Instant earliestAllowed = now.minus(Duration.ofHours(properties.getMaxCatchUpHours()));
        Instant start = latest.minus(overlap);
        return start.isBefore(earliestAllowed) ? earliestAllowed : start;
    }

    /** Slaat alleen metingen op die er nog niet zijn (sleutel: station + tijdstip). Geeft het aantal nieuwe terug. */
    @Transactional
    public int store(List<Measurement> fetched) {
        if (fetched.isEmpty()) {
            return 0;
        }
        Instant min = fetched.get(0).getTimestamp();
        Instant max = min;
        Set<Long> stationIds = new HashSet<>();
        for (Measurement m : fetched) {
            if (m.getTimestamp().isBefore(min)) {
                min = m.getTimestamp();
            }
            if (m.getTimestamp().isAfter(max)) {
                max = m.getTimestamp();
            }
            stationIds.add(m.getStation().getStationid());
        }

        Set<String> existing = new HashSet<>();
        for (var key : measurementRepository.findKeysBetween(min, max)) {
            existing.add(key.getStationId() + "@" + key.getTimestamp());
        }

        Map<Long, Station> stations = new HashMap<>();
        stationRepository.findAllById(stationIds).forEach(s -> stations.put(s.getStationid(), s));

        List<Measurement> toSave = new ArrayList<>();
        for (Measurement m : fetched) {
            Long stationId = m.getStation().getStationid();
            // Ook binnen de batch ontdubbelen: add() geeft false bij een tweede keer.
            if (!existing.add(stationId + "@" + m.getTimestamp())) {
                continue;
            }
            m.setStation(stations.computeIfAbsent(stationId, id -> stationRepository.save(newStation(id))));
            toSave.add(m);
        }
        measurementRepository.saveAll(toSave);
        return toSave.size();
    }

    // Een station dat de bron kent maar wij nog niet. Registratiecode negatief en uniek, zodat
    // het nooit botst met een echte code; vlaggen op false zodat de monitor er niet over struikelt.
    private static Station newStation(Long stationId) {
        Station station = new Station();
        station.setStationid(stationId);
        station.setName("MJS");
        station.setIsActive(true);
        station.setIs_public(false);
        station.setRegistrationCode(-stationId);
        station.setTempError(false);
        station.setHumError(false);
        station.setStofError(false);
        station.setLocError(false);
        return station;
    }
}
