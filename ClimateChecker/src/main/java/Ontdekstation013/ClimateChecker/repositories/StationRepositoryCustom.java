package Ontdekstation013.ClimateChecker.repositories;

import Ontdekstation013.ClimateChecker.models.Station;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepositoryCustom extends StationRepository{

    List<Station> findAllByOwner_UserID(long userId);


    Optional<Station> findByRegistrationCodeAndDatabaseTag(long registrationCode, String databaseTag);
}
