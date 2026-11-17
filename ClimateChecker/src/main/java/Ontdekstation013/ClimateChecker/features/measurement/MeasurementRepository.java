package Ontdekstation013.ClimateChecker.features.measurement;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    /**
     * Alle metingen in het venster [start, end] (grenzen inclusief). Levert platte rijen met het
     * station-id in plaats van het station zelf, zodat er geen lazy loading per meting nodig is.
     */
    @Query("select new Ontdekstation013.ClimateChecker.features.measurement.MeasurementRow("
            + "m.id, m.station.stationid, m.timestamp, m.latitude, m.longitude, "
            + "m.temperature, m.humidity, m.pm25, m.pm10, m.locationInherited) "
            + "from Measurement m "
            + "where m.station is not null and m.timestamp >= :start and m.timestamp <= :end "
            + "order by m.timestamp, m.id")
    List<MeasurementRow> findRowsBetween(@Param("start") Instant start, @Param("end") Instant end);

    /** Zoals {@link #findRowsBetween}, maar beperkt tot de gegeven stations. */
    @Query("select new Ontdekstation013.ClimateChecker.features.measurement.MeasurementRow("
            + "m.id, m.station.stationid, m.timestamp, m.latitude, m.longitude, "
            + "m.temperature, m.humidity, m.pm25, m.pm10, m.locationInherited) "
            + "from Measurement m "
            + "where m.station.stationid in :stationIds and m.timestamp >= :start and m.timestamp <= :end "
            + "order by m.timestamp, m.id")
    List<MeasurementRow> findRowsForStationsBetween(@Param("stationIds") List<Long> stationIds,
                                                    @Param("start") Instant start, @Param("end") Instant end);

    /** Sleutels (station, tijdstip) die al bestaan in het venster, voor idempotent opslaan. */
    @Query("select m.station.stationid as stationId, m.timestamp as timestamp from Measurement m "
            + "where m.station is not null and m.timestamp >= :start and m.timestamp <= :end")
    List<MeasurementKey> findKeysBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("select max(m.timestamp) from Measurement m")
    Instant findLatestTimestamp();

    /** Laatste meettijdstip per station, ook voor stations die al lang niets meer sturen. */
    @Query("select m.station.stationid as stationId, max(m.timestamp) as timestamp from Measurement m "
            + "where m.station is not null group by m.station.stationid")
    List<MeasurementKey> findLastTimestampPerStation();

    /** Sleutel van een meting: station en tijdstip. */
    interface MeasurementKey {
        Long getStationId();

        Instant getTimestamp();
    }
}
