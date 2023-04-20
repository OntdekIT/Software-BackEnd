package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.Station;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.repositories.StationRepository;
import Ontdekstation013.ClimateChecker.Services.SensorService;
import Ontdekstation013.ClimateChecker.Services.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/Station")
public class StationController {

    private final StationService stationService;
    private final SensorService sensorService;

    @Autowired
    public StationController(StationService stationService, SensorService sensorService){
        this.stationService = stationService;
        this.sensorService = sensorService;
    }

    // get station based on id
    @GetMapping("{stationId}")
    public ResponseEntity<stationDto> getStationById(@PathVariable long stationId) {
        stationDto dto = stationService.findStationById(stationId);
        return ResponseEntity.ok(dto);
    }

    // get stations by user id
    @GetMapping("user/{userId}")
    public ResponseEntity<List<stationTitleDto>> getStationsByUserId(@PathVariable long userId) {

        List<stationTitleDto> newDtoList = stationService.getAllByUserId(userId);
        return ResponseEntity.ok(newDtoList);
    }

    // get all by page number
    @GetMapping("page/{pageNumber}")
    public ResponseEntity<List<stationTitleDto>> getAllStationsByPage(@PathVariable long pageId){

        List<stationTitleDto> newDtoList = stationService.getAllByPageId(pageId);
        return ResponseEntity.ok(newDtoList);
    }

    // get all stations
    @GetMapping
    public ResponseEntity<List<stationTitleDto>> getAllStations() {

        List<stationTitleDto> newDtoList = stationService.getAll();
        return ResponseEntity.ok(newDtoList);
    }

    //get all stations for map
    @GetMapping("Stations")
    public ResponseEntity<List<stationDto>> getAllStationsMap() {

        List<stationDto> newDtoList = stationService.getAllStations();

//        for(stationDto dto : newDtoList){
//            dto.setSensors(sensorService.getSensorsByStation(dto.getId()));
//        }

        return ResponseEntity.ok(newDtoList);
    }

    // create new station
    @PostMapping
    public ResponseEntity<stationDto> createStation(@RequestBody registerStationDto stationDto){

        boolean created = stationService.createStation(stationDto);

        if (created) {
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    // delete station
    @DeleteMapping("{stationId}")
    public ResponseEntity<stationDto> deleteStation(@PathVariable long stationId){

        stationService.deleteStation(stationId);
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(null);
    }

    // edit station
    @PutMapping
    public ResponseEntity<stationDto> editStation(@RequestBody editStationDto stationDto){

        stationService.editStation(stationDto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
