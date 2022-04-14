package Ontdekstation013.ClimateChecker.repositories;

import Ontdekstation013.ClimateChecker.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {
}
