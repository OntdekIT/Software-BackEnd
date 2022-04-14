package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.models.Sensor;
import Ontdekstation013.ClimateChecker.repositories.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;

    @Autowired
    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    public Sensor findSensorById(Long id) {
        Sensor sensor = sensorRepository.findById(id).get();
        return sensor;
    }
}
