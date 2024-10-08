package Ontdekstation013.ClimateChecker.features.station;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IStationRepository extends JpaRepository <Station, Long> {
    Station getByRegistrationCode(Long meetstationCode);
    Station getMeetstationByStationid(Long id);
}
