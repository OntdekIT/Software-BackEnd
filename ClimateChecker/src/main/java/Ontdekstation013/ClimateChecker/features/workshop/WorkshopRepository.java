package Ontdekstation013.ClimateChecker.features.workshop;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {
    Workshop findByCode(Long code);
    List<Workshop> findByExpirationDateBefore(LocalDateTime now);
}
