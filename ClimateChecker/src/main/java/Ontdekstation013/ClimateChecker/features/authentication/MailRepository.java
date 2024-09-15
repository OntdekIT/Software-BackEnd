package Ontdekstation013.ClimateChecker.features.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface MailRepository extends JpaRepository<Mail, Integer> {
}

