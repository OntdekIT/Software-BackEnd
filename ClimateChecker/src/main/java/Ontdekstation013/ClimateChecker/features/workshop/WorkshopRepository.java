package Ontdekstation013.ClimateChecker.features.workshop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {
    Workshop findByCode(Long code);
    List<Workshop> findByExpirationDateBefore(LocalDateTime now);
}
