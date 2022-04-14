package Ontdekstation013.ClimateChecker.repositories;

import Ontdekstation013.ClimateChecker.models.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
