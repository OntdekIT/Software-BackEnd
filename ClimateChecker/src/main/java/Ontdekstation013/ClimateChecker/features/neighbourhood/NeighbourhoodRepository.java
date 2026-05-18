package Ontdekstation013.ClimateChecker.features.neighbourhood;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighbourhoodRepository extends JpaRepository<Neighbourhood, Long> { }