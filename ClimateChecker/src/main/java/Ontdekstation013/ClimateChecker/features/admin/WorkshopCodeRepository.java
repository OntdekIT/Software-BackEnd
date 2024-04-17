package Ontdekstation013.ClimateChecker.features.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Ontdekstation013.ClimateChecker.features.user.User;
@Repository

public interface WorkshopCodeRepository extends JpaRepository<WorkshopCode, Long> {
    WorkshopCode findByRandomCode(String code);
}
