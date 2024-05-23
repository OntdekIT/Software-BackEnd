package Ontdekstation013.ClimateChecker.features.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByMailAddress(String mailAddress);
    boolean existsUserByUserIDAndAdminTrue(Long id);
    User findByMailAddress(String mail);
}
