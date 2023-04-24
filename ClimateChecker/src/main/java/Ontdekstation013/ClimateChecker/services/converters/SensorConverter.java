package Ontdekstation013.ClimateChecker.services.converters;

import Ontdekstation013.ClimateChecker.models.Sensor;
import Ontdekstation013.ClimateChecker.models.dto.sensorDto;

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
