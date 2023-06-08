package Ontdekstation013.ClimateChecker.repositories;

import Ontdekstation013.ClimateChecker.models.Sensor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepositoryCustom extends SensorRepository{

    List<Sensor> findAllBySensorType_TypeID(long typeId);
    List<Sensor> findByStation_StationID(long stationId);

    Optional<List<Sensor>> findAllByActiveData(boolean activeDataInput);

}
