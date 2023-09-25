package Ontdekstation013.ClimateChecker.features.sensor;

import Ontdekstation013.ClimateChecker.features.sensor.endpoint.sensorDto;

import org.springframework.stereotype.Service;

@Service
public class SensorConverter {


    public sensorDto sensorToSensorDTO(Sensor sensor){
        sensorDto newdto = new sensorDto();
        newdto.setId(sensor.getSensorID());
        newdto.setStationId(sensor.getStation().getStationID());
        newdto.setData(sensor.getSensorData());
        newdto.setTypeId(sensor.getSensorType().getTypeID());

        return newdto;
    }
}
