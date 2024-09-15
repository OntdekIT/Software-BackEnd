package Ontdekstation013.ClimateChecker.features.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository

public interface WorkshopCodeRepository extends JpaRepository<WorkshopCode, Long> {
    WorkshopCode findByCode(Long code);
    List<WorkshopCode> findAllByExpirationDateBefore(LocalDateTime date);


}
