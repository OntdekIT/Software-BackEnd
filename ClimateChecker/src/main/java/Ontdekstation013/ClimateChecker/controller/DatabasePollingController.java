package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.dto.stationDto;
import Ontdekstation013.ClimateChecker.Services.DatabasePollingService;
import Ontdekstation013.ClimateChecker.Services.LocationService;
import Ontdekstation013.ClimateChecker.Services.SensorService;
import Ontdekstation013.ClimateChecker.Services.StationService;
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

    @Autowired
    public DatabasePollingController(SensorService sensorService, StationService stationService, LocationService locationService, DatabasePollingService databasePollingService) {
        this.sensorService = sensorService;
        this.stationService = stationService;
        this.locationService = locationService;
        this.databasePollingService = databasePollingService;
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


    @PostMapping("/createStation/{databaseTag}/{registrationCode}")
    public ResponseEntity<String> createStationByRegistrationCode(@PathVariable String databaseTag, @PathVariable long registrationCode){

        stationDto station = stationService.findStationByRegistrationCode(registrationCode, databaseTag);

        if (station != null){



            return new ResponseEntity<>("Station has been added to the database", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Unable to add station to the database", HttpStatus.ACCEPTED);
    }
}
