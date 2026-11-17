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
}
