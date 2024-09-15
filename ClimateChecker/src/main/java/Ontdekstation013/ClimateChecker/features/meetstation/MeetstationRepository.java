package Ontdekstation013.ClimateChecker.features.meetstation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetstationRepository extends JpaRepository <Meetstation, Long> {
    Meetstation getByRegistrationCode(Long meetstationCode);

    Meetstation findMeetstationByStationid(Long id);
    Meetstation getMeetstationByStationid(Long id);
}
