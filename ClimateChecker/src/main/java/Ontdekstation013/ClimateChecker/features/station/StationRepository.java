package Ontdekstation013.ClimateChecker.features.station;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository <Station, Long> {
    Station getByRegistrationCode(Long meetstationCode);

    Station findMeetstationByStationid(Long id);
    Station getMeetstationByStationid(Long id);
}
