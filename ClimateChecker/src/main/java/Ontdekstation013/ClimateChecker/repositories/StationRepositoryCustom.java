package Ontdekstation013.ClimateChecker.repositories;

import Ontdekstation013.ClimateChecker.models.Station;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepositoryCustom extends StationRepository{

    List<Station> findAllByOwner_UserID(long userId);


    List<Station> findAllByRegistrationCodeAndDatabaseTag(long registrationCode, String databaseTag);
}
