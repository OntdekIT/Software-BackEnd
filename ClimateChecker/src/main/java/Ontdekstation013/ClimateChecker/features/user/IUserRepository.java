package Ontdekstation013.ClimateChecker.features.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface IUserRepository extends JpaRepository<User, Long> {
    boolean existsUserByMailAddress(String mailAddress);
    User findByMailAddress(String mail);
}