package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.workshopCode.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByEmail(String email);
    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR u.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR u.lastName LIKE %:lastName%) AND " +
            "(:email IS NULL OR u.email LIKE %:email%) AND " +
            "(:isAdmin IS NULL OR u.isAdmin = :isAdmin)")
    List<User> findUsersByOptionalFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("isAdmin") Boolean isAdmin);

    List<User> findByWorkshopCode(Workshop workshop);
}
