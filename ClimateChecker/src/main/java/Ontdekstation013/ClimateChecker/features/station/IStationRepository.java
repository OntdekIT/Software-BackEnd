package Ontdekstation013.ClimateChecker.features.station;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IStationRepository extends JpaRepository <Station, Long> {
    Station getByRegistrationCode(Long meetstationCode);
    Station getMeetstationByStationid(Long id);
    public List<Station> findByUserid(Long userid);
}
