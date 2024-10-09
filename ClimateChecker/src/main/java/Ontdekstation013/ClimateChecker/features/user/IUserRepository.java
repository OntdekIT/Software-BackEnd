package Ontdekstation013.ClimateChecker.features.user;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface IUserRepository extends JpaRepository<User, Long> {
    boolean existsUserByMailAddress(String mailAddress);
    User findByMailAddress(String mail);


}
