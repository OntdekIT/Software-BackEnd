package Ontdekstation013.ClimateChecker.features.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    boolean existsUserByEmail(String email);
    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR u.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR u.lastName LIKE %:lastName%) AND " +
            "(:mailAddress IS NULL OR u.mailAddress LIKE %:mailAddress%) AND " +
            "(:admin IS NULL OR u.Admin = :admin)")
    List<User> findUsersByOptionalFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("mailAddress") String mailAddress,
            @Param("admin") Boolean admin);

}
