package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.models.Sensor;
import Ontdekstation013.ClimateChecker.models.SensorType;
import Ontdekstation013.ClimateChecker.models.Station;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.repositories.*;
import Ontdekstation013.ClimateChecker.services.converters.SensorConverter;
import Ontdekstation013.ClimateChecker.services.converters.StationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SensorService {

    private final SensorRepositoryCustom sensorRepository;
    private TypeRepository typeRepository;

    private SensorConverter sensorConverter;
    private StationRepositoryCustom stationRepository;



    public static double avgformat(double num) {
        /*DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();*/
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator('.');
        DecimalFormat dfSharp = new DecimalFormat("#.##", formatSymbols);
        double newAverage = Double.parseDouble(dfSharp.format(num));
        return newAverage;
    }

    @Autowired
    public SensorService(SensorRepositoryCustom sensorRepository, TypeRepository typeRepository, SensorConverter sensorConverter, StationRepositoryCustom stationRepository) {
        this.sensorRepository = sensorRepository;
        this.typeRepository = typeRepository;
        this.sensorConverter = sensorConverter;
        this.stationRepository = stationRepository;
    }


    public sensorDto findSensorById(long id) {
        Sensor sensor = sensorRepository.findById(id).get();
        sensorDto newdto = sensorConverter.sensorToSensorDTO(sensor);
        return newdto;
    }

    // not yet functional
    public List<sensorDto> getAll() {
        Iterable<Sensor> SensorList = sensorRepository.findAll();
        List<sensorDto> newDtoList = new ArrayList<>();
        for (Sensor sensor: SensorList
        ) {
            newDtoList.add(sensorConverter.sensorToSensorDTO(sensor));
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
            double avgRounded = avgformat(avgData);

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
            newDtoList.add(sensorConverter.sensorToSensorDTO(sensor));
        }

        return newDtoList;
    }

    // not yet functional
    public List<sensorDto> getSensorsByStationId(long stationId) {
        Iterable<Sensor> sensorList = sensorRepository.findByStation_StationID(stationId);

        List<sensorDto> newDtoList = new ArrayList<>();
        for (Sensor sensor: sensorList) {

            if (sensor.getStation().getStationID() == stationId)
            newDtoList.add(sensorConverter.sensorToSensorDTO(sensor));

        }


        return newDtoList;
    }


    public boolean addSensorData(MeetJeStadDto meetJeStadDto){

        Station station = stationRepository.findByRegistrationCodeAndDatabaseTag(meetJeStadDto.id, "MJS").orElse(null);

        boolean succes = false;
        try{
            SensorType temperatureType =  new SensorType(1L, "Temperatuur");
            Sensor temperatureSensor = new Sensor((int)meetJeStadDto.temperature, temperatureType,station);
            SensorType humidityType = new SensorType(5L, "Luchtvochtigheid");
            Sensor humiditySensor = new Sensor((int)meetJeStadDto.humidity, humidityType, station);
            SensorType supplyType = new SensorType(7L, "Batterij");
            Sensor supplySensor = new Sensor((int)meetJeStadDto.supply, supplyType, station);

            sensorRepository.save(temperatureSensor);
            sensorRepository.save(humiditySensor);
            sensorRepository.save(supplySensor);

            succes = true;
        }catch (Exception ex){

        }
        return succes;
    }



    // not yet functional
    public void createSensor(sensorDto sensorDto) {

    }

    public void createSensorType(sensorTypeDto sensorTypeDto) {
    
    }


    public void deleteSensor(long sensorId) {
    }
}
