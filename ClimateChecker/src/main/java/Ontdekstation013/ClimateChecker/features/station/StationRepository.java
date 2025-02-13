package Ontdekstation013.ClimateChecker.features.station;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository <Station, Long> {
    Station getByRegistrationCode(Long meetstationCode);
    Station getMeetstationByStationid(Long id);
    List<Station> findByUserid(Long userid);
}
