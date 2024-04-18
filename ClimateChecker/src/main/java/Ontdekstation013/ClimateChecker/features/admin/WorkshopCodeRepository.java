package Ontdekstation013.ClimateChecker.features.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface WorkshopCodeRepository extends JpaRepository<WorkshopCode, Long> {
    WorkshopCode findByCode(Long code);


}
