package Ontdekstation013.ClimateChecker.features.workshopcode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IWorkshopCodeRepository extends JpaRepository<WorkshopCode, Long> {
    List<WorkshopCode> findByExpirationDateBefore(LocalDateTime now);
}
