package Ontdekstation013.ClimateChecker.controller;
import java.util.List;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.services.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/Sensor")

public class SensorController {

    private final SensorService sensorService;
    @Autowired
    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    // get sensor based on id
    @GetMapping("{sensorId}")
    public ResponseEntity<sensorDto> getSensorById(@PathVariable long sensorId) {
        sensorDto dto = sensorService.findSensorById(sensorId);
        return ResponseEntity.ok(dto);
    }

    // get all stations
    @GetMapping
    public ResponseEntity<List<sensorDto>> getAllSensors(){

        List<sensorDto> newDtoList = sensorService.getAll();
        return ResponseEntity.ok(newDtoList);
    }

    // get all sensorTypes
    @GetMapping("type")
    public ResponseEntity<List<sensorTypeDto>> getAllSensorTypes(){

        List<sensorTypeDto> newDtoList = sensorService.getAllSensorTypes();
        return ResponseEntity.ok(newDtoList);
    }

    // get all sensors by type
    @GetMapping("type/{typeId}")
    public ResponseEntity<List<sensorDto>> getSensorsByType(@PathVariable long typeId) {
        List<sensorDto> dto = sensorService.getSensorsByType(typeId);
        return ResponseEntity.ok(dto);
    }

    // get the average data of each sensor type
    @GetMapping("average")
    public ResponseEntity<sensorAverageDto> getSensorAverage(){
        sensorAverageDto avgDto = sensorService.getAllAverageSensorData();
        return ResponseEntity.ok(avgDto);
    }

    // get all sensors by station
    @GetMapping("station/{stationId}")
    public ResponseEntity<List<sensorDto>> getSensorsByStation(@PathVariable long stationId) {
        List<sensorDto> dto = sensorService.getSensorsByStation(stationId);
        return ResponseEntity.ok(dto);
    }

    // create new sensor
    @PostMapping
    public ResponseEntity<sensorDto> createSensor(@RequestBody sensorDto sensorDto){

        sensorService.createSensor(sensorDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }


    // create new sensor type
    @PostMapping("newType")
    public ResponseEntity<sensorTypeDto> createSensorType(@RequestBody sensorTypeDto sensorTypeDto){

        sensorService.createSensorType(sensorTypeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }


    // delete sensor
    @DeleteMapping("{sensorId}")
    public ResponseEntity<sensorDto> deleteSensor(@PathVariable long sensorId){

        sensorService.deleteSensor(sensorId);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

}
