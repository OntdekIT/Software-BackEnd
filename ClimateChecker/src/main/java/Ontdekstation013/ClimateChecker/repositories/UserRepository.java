package Ontdekstation013.ClimateChecker.repositories;

import Ontdekstation013.ClimateChecker.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByUserName(String userName);
    boolean existsUserByMailAddress(String mailAddress);
    User findByMailAddress(String mail);
}
