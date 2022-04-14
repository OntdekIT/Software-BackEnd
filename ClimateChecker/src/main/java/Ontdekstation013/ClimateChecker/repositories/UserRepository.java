package Ontdekstation013.ClimateChecker.repositories;

import Ontdekstation013.ClimateChecker.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
