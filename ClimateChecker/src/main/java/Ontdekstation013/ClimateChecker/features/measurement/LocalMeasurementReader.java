package Ontdekstation013.ClimateChecker.features.measurement;

import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.utility.MeasurementDeduplicator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Leest metingen uit de lokale database (gevuld door de synchronisatie) in plaats van live bij
 * meetjestad.net. Past dezelfde nabewerking toe als de live-route: ontdubbelen op
 * (station, tijdstip) en, tenzij defecte metingen gevraagd zijn, het uitschieterfilter.
 *
 * <p>De stations worden eenmalig in een map geladen, dus het aantal queries hangt niet af van
 * het aantal metingen.
 */
@Service
@RequiredArgsConstructor
public class LocalMeasurementReader {
    private final MeasurementRepository measurementRepository;
    private final StationRepository stationRepository;
    private final MeetJeStadService meetJeStadService;

    public List<Measurement> getMeasurements(Instant start, Instant end, boolean includeFaultyMeasurements) {
        List<MeasurementRow> rows = measurementRepository.findRowsBetween(start, end);
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Station> stationsById = new HashMap<>();
        for (Station station : stationRepository.findAll()) {
            stationsById.put(station.getStationid(), station);
        }

        List<Measurement> measurements = new ArrayList<>(rows.size());
        for (MeasurementRow row : rows) {
            measurements.add(toMeasurement(row, stationsById));
        }

        measurements = MeasurementDeduplicator.deduplicate(measurements);
        return includeFaultyMeasurements ? measurements : meetJeStadService.IncorrectValueFilter(measurements);
    }

    private static Measurement toMeasurement(MeasurementRow row, Map<Long, Station> stationsById) {
        Measurement measurement = new Measurement(row.timestamp(), row.latitude(), row.longitude(),
                row.temperature(), row.humidity(), row.pm25(), row.pm10());
        measurement.setLocationInherited(row.locationInherited());
        measurement.setStation(stationsById.computeIfAbsent(row.stationId(), LocalMeasurementReader::unknownStation));
        return measurement;
    }

    // Zelfde placeholder als de live-route gebruikte voor een station dat niet in de database staat.
    private static Station unknownStation(Long stationId) {
        Station station = new Station();
        station.setStationid(stationId);
        station.setName("Unknown Station " + stationId);
        station.setIsActive(false);
        station.setIs_public(false);
        station.setRegistrationCode(0L);
        station.setMeasurementList(new ArrayList<>());
        return station;
    }
}
