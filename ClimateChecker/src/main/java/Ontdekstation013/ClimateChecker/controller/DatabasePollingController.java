package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.dto.MeetJeStadDto;
import Ontdekstation013.ClimateChecker.models.dto.createLocationDto;
import Ontdekstation013.ClimateChecker.models.dto.createStationDto;
import Ontdekstation013.ClimateChecker.models.dto.stationDto;
import Ontdekstation013.ClimateChecker.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/Data")
public class DatabasePollingController {

    private final SensorService sensorService;
    private final StationService stationService;
    private final LocationService locationService;
    private final DatabasePollingService databasePollingService;
    private final MJSValidationService MJSvalidationService;

    @Autowired
    public DatabasePollingController(SensorService sensorService, StationService stationService, LocationService locationService, DatabasePollingService databasePollingService, MJSValidationService MJSvalidationService) {
        this.sensorService = sensorService;
        this.stationService = stationService;
        this.locationService = locationService;
        this.databasePollingService = databasePollingService;
        this.MJSvalidationService = MJSvalidationService;
    }

//    @GetMapping("/get")
//    public ResponseEntity<String> GetAllStationData(){
//        //Get the registration codes > Station Repos
//        //List<Long> registrationCodes = stationService.GetAllRegistrationCodes();
//
//        //Use registration codes to build a query for MeetJeStad
//        //For all the StationIDs/Registration codes, parse the sensor data
//        //Save the sensor data to the sensor repos.
//
//
//    }


    @PostMapping("/createStation")
    public ResponseEntity<String> createStationByRegistrationCode(@RequestParam("databaseTag") String databaseTag, @RequestParam("registrationCode") long registrationCode){

        stationDto station = stationService.findStationByRegistrationCode(registrationCode, databaseTag);

        if (station == null){
            MeetJeStadDto MJSDto = databasePollingService.GetStationToRegister(registrationCode);
            MJSDto = MJSvalidationService.ValidateDTO(MJSDto);

            createLocationDto createLocationDto = new createLocationDto();
            long locationId = locationService.createLocation(createLocationDto);

            createStationDto createStationDto = new createStationDto();
            createStationDto.setLocationId(locationId);
            createStationDto.setRegistrationCode(registrationCode);
            createStationDto.setDatabaseTag(databaseTag);
            createStationDto.setLongitude(MJSDto.longitude);
            createStationDto.setLatitude(MJSDto.latitude);
            stationService.createStation(createStationDto);

            return new ResponseEntity<>("Station has been added to the database", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Unable to add station to the database", HttpStatus.ACCEPTED);
    }
}
