package Ontdekstation013.ClimateChecker.features.neighbourhood;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface INeighbourhoodRepository extends JpaRepository<Neighbourhood, Long> {

}
