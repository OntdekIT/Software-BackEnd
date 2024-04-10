package Ontdekstation013.ClimateChecker.features.user;

import ch.qos.logback.core.net.SMTPAppenderBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByMailAddress(String mailAddress);
    User findByMailAddress(String mail);

    String getPasswordByMailAddress(String mailAddress);
}
