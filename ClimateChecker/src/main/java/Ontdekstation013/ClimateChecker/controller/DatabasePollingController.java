package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.Station;
import Ontdekstation013.ClimateChecker.models.dto.MeetJeStadCreateStationDto;
import Ontdekstation013.ClimateChecker.models.dto.stationDto;
import Ontdekstation013.ClimateChecker.services.DatabasePollingService;
import Ontdekstation013.ClimateChecker.services.SensorService;
import Ontdekstation013.ClimateChecker.services.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/Data")
public class DatabasePollingController {

    private final SensorService sensorService;
    private final StationService stationService;
    private final DatabasePollingService databasePollingService;

    @Autowired
    public DatabasePollingController(SensorService sensorService, StationService stationService, DatabasePollingService databasePollingService) {
        this.sensorService = sensorService;
        this.stationService = stationService;
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


    @PostMapping("/createStation/{registrationCode}")
    public ResponseEntity<String> createStationByRegistrationCode(@PathVariable long registrationCode){
        stationDto station = stationService.findStationByRegistrationCode(registrationCode);

        if (station == null){
            String query = databasePollingService.BuildQueryCreateStationFromMeetJeStad(registrationCode);
            RestTemplate restTemplate = new RestTemplate();

            // Zo hoort het te werken
            //ResponseEntity<MeetJeStadCreateStationDto> response = restTemplate.getForEntity(query, MeetJeStadCreateStationDto.class);
            //MeetJeStadCreateStationDto stationResponse = response.getBody();

            // Nu doen we het zo
            ResponseEntity<String> response = restTemplate.getForEntity(query, String.class);
            MeetJeStadCreateStationDto MJSstation = new MeetJeStadCreateStationDto();
            MJSstation.GetValuesFromJSONString(response.getBody());



//            stationService.createStation()

            return new ResponseEntity<>("Station has been added to the database", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Unable to add station to the database", HttpStatus.ACCEPTED);
    }
}
