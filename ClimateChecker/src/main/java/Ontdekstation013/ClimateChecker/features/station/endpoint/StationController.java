package Ontdekstation013.ClimateChecker.features.station.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.station.endpoint.dto.UpdateStationRequest;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/Meetstation")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class StationController {
    private final StationService stationService;
    private final MeasurementService measurementService;
    private final WorkshopService adminService;

    public StationController(StationService stationService, MeasurementService measurementService, WorkshopService workshopService) {
        this.stationService = stationService;
        this.measurementService = measurementService;
        this.adminService = workshopService;
    }

    @GetMapping("{id}")
    public ResponseEntity<StationDto> getMeetstation(@PathVariable Long id) throws Exception {
        try {
            if (id != 0) {
                StationDto stationDto = stationService.ReadById(id);
                return ResponseEntity.status(HttpStatus.OK).body(stationDto);
            }
        } catch (Exception error) {

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateMeetstation(@PathVariable long id, @RequestBody UpdateStationRequest updaterequest) throws Exception {
        Station station = stationService.GetStationById(id);
        station.setIs_public(updaterequest.is_public());
        if (updaterequest.name() != null && !updaterequest.name().isEmpty()) {
            station.setName(updaterequest.name());
        }
        stationService.editstation(id, station);
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @GetMapping("/measurements/{id}")
    public ResponseEntity<byte[]> getMeetstationMeasurements(@PathVariable int id, @RequestParam String startDate, @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            LocalDateTime localDateTimeStart = LocalDateTime.parse(startDate, formatter);
            Instant startInstant = localDateTimeStart.atZone(ZoneId.systemDefault()).toInstant();

            LocalDateTime localDateTimeEnd = LocalDateTime.parse(endDate, formatter);
            Instant endInstant = localDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant();

            if (startInstant.isAfter(endInstant)) {
                throw new InvalidArgumentException("Start date is after end date");
            }
            byte[] pdfBytes = measurementService.getMeasurementsAsPDF(id, startInstant, endInstant);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "measurements.pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @GetMapping("/Availibility/{stationId}/{workshopCode}")
    public ResponseEntity<Integer> IsAvailable(@PathVariable Long stationId, @PathVariable Long workshopCode) {
        try {
            if (!stationService.IsAvailable(stationId)) {
                return ResponseEntity.status(HttpStatus.OK).body(402);
            } else if (!adminService.verifyWorkshopCode(workshopCode)) {
                return ResponseEntity.status(HttpStatus.OK).body(403);
            }
            return ResponseEntity.status(HttpStatus.OK).body(200);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/Claim")
    public ResponseEntity<String> ClaimStation(@RequestBody StationDto stationDto, HttpServletRequest request) {
        try {
            Cookie[] cookies;
            if (request.getCookies() != null) {
                cookies = request.getCookies();
                Long userID = Long.parseLong(cookies[0].getValue());
                stationDto.userid = userID;
                stationService.ClaimMeetstation(new Station(stationDto));
            }
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // Create station endpoint salvaged from removed DatabasePollingController
    // TODO: Refactor this endpoint to use the current architecture
//    @PostMapping("/createStation")
//    public ResponseEntity<String> createStationByRegistrationCode(@RequestParam("databaseTag") String databaseTag, @RequestParam("registrationCode") long registrationCode){
//
//        stationDto station = stationService.findStationByRegistrationCode(registrationCode, databaseTag);
//
//        if (station == null){
//            MeetJeStadDto MJSDto = databasePollingService.GetStationToRegister(registrationCode);
//            MJSDto = MJSvalidationService.ValidateRegisterDTO(MJSDto);
//
//            createLocationDto createLocationDto = new createLocationDto();
//            createLocationDto.setLongitude(MJSDto.longitude);
//            createLocationDto.setLatitude(MJSDto.latitude);
//            long locationId = locationService.createLocation(createLocationDto);
//
//            createStationDto createStationDto = new createStationDto();
//            createStationDto.setLocationId(locationId);
//            createStationDto.setRegistrationCode(registrationCode);
//            createStationDto.setDatabaseTag(databaseTag);
//            stationService.createStation(createStationDto);
//
//            return new ResponseEntity<>("Station has been added to the database", HttpStatus.ACCEPTED);
//        }
//        return new ResponseEntity<>("Unable to add station to the database", HttpStatus.NOT_ACCEPTABLE);
//    }
}
