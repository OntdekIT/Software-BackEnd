package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.models.Sensor;
import Ontdekstation013.ClimateChecker.models.SensorType;
import Ontdekstation013.ClimateChecker.models.dto.sensorDto;
import Ontdekstation013.ClimateChecker.models.dto.sensorAverageDto;
import Ontdekstation013.ClimateChecker.models.dto.sensorTypeDto;
import Ontdekstation013.ClimateChecker.repositories.SensorRepository;
import Ontdekstation013.ClimateChecker.repositories.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private TypeRepository typeRepository;

    private static final DecimalFormat dfSharp = new DecimalFormat("#.##");

    @Autowired
    public SensorService(SensorRepository sensorRepository, TypeRepository typeRepository) {
        this.sensorRepository = sensorRepository;
        this.typeRepository = typeRepository;
    }


    public sensorDto findSensorById(long id) {
        Sensor sensor = sensorRepository.findById(id).get();
        sensorDto newdto = sensorToSensorDTO(sensor);
        return newdto;
    }

    public sensorDto sensorToSensorDTO(Sensor sensor){
        sensorDto newdto = new sensorDto();
        newdto.setId(sensor.getSensorID());
        newdto.setStationId(sensor.getStation().getStationID());
        newdto.setData(sensor.getSensorData());
        newdto.setTypeId(sensor.getSensorType().getTypeID());

        return newdto;
    }

    // not yet functional
    public List<sensorDto> getAll() {
        Iterable<Sensor> SensorList = sensorRepository.findAll();
        List<sensorDto> newDtoList = new ArrayList<>();
        for (Sensor sensor: SensorList
        ) {
            newDtoList.add(sensorToSensorDTO(sensor));
        }


        return newDtoList;
    }

    // not yet functional
    public List<sensorTypeDto> getAllSensorTypes() {

        List<sensorTypeDto> newDtoList = new ArrayList();
        Iterable<SensorType> sensorTypes = typeRepository.findAll();


        for (SensorType type:sensorTypes
             ) {
            sensorTypeDto newdto = new sensorTypeDto();

            newdto.setId(type.getTypeID());
            newdto.setName(type.getTypeName());

            newDtoList.add(newdto);
        }


        return newDtoList;
    }

    // return the average data of every sensor type
    public sensorAverageDto getAllAverageSensorData() {
        sensorAverageDto avgDto = new sensorAverageDto();

        //get all types
        Iterable<SensorType> sensorTypes = typeRepository.findAll();

        //foreach type
        for (SensorType type: sensorTypes){
            // get all sensor values per sensor type
            List<sensorDto> sensors = getSensorsByType(type.getTypeID());

            //calculate the average for each type
            double avgData = 0;
                //type var: array
            for (sensorDto sensor : sensors) {
                avgData += sensor.getData();
            }
            avgData /= sensors.size();
            double avgRounded = Double.parseDouble(dfSharp.format(avgData));

            switch ((int) type.getTypeID()) {
                case 1:
                    avgDto.setTemperature(avgRounded);
                    break;
                case 2:
                    avgDto.setNitrogen(avgRounded);
                    break;
                case 3:
                    avgDto.setCarbonDioxide(avgRounded);
                    break;
                case 4:
                    avgDto.setParticulateMatter(avgRounded);
                    break;
                case 5:
                    avgDto.setHumidity(avgRounded);
                    break;
                case 6:
                    avgDto.setWindSpeed(avgRounded);
                    break;
            }
        }
        //return all the averages
        return avgDto;
    }

    // gets all sensors by specific type
    public List<sensorDto> getSensorsByType(long typeId) {
        Iterable<Sensor> sensorList = sensorRepository.findAll();

        List<sensorDto> newDtoList = new ArrayList<>();
        for (Sensor sensor: sensorList
        ) {
            if (sensor.getSensorType().getTypeID() == typeId)
            newDtoList.add(sensorToSensorDTO(sensor));
        }

        return newDtoList;
    }

    // not yet functional
    public List<sensorDto> getSensorsByStation(long stationId) {
        Iterable<Sensor> sensorList = sensorRepository.findAllByStation(stationId);

        List<sensorDto> newDtoList = new ArrayList<>();
        for (Sensor sensor: sensorList
        ) {

            if (sensor.getStation().getStationID() == stationId)
            newDtoList.add(sensorToSensorDTO(sensor));

        }


        return newDtoList;
    }

    // not yet functional
    public void createSensor(sensorDto sensorDto) {

    }

    public void createSensorType(sensorTypeDto sensorTypeDto) {
    
    }


    public void deleteSensor(long sensorId) {
    }
}
