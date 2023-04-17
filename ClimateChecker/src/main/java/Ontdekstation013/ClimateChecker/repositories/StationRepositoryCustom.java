package Ontdekstation013.ClimateChecker.repositories;

import Ontdekstation013.ClimateChecker.models.Station;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StationRepositoryCustom {

    @Query(value = "SELECT * FROM STATION WHERE USERID = ?1", nativeQuery = true)
    List<Station> findAllByUserId(long userId);

    @Query(value = "SELECT * FROM STATION WHERE REGISTRATION_CODE = ?1", nativeQuery = true)
    List<Station> findAllByRegistrationCode(long registrationCode);
}
