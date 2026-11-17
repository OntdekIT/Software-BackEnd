package Ontdekstation013.ClimateChecker.features.station.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationMapper;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.station.endpoint.dto.GetAllStationsRequest;
import Ontdekstation013.ClimateChecker.features.station.endpoint.dto.UpdateStationRequest;
import Ontdekstation013.ClimateChecker.features.station.endpoint.dto.TransferStationRequest;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    public ResponseEntity<StationDto> getMeetstation(@PathVariable Long id) {
        StationDto stationDto = id != 0 ? stationService.ReadById(id) : null;
        if (stationDto == null) {
            throw new NotFoundException("Station not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(stationDto);
    }

    @GetMapping("/stations")
    public ResponseEntity<?> getAllMeetstations(GetAllStationsRequest request) {
        List<Station> stations = stationService.getAllStations(StationMapper.toStationFilter(request));
        List<StationDto> responses = new ArrayList<>();

        for (Station station : stations) {
            responses.add(StationMapper.toStationDTO(station));
        }

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stationsMetMeasurements")
    public CompletableFuture<ResponseEntity<List<StationDto>>> getMeetstationWithMeasurement(
            @RequestParam(value = "timestamp") String timestamp) {
        Instant utcDateTime;
        try {
            utcDateTime = Instant.parse(timestamp);
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Timestamp must be in ISO 8601 format");
        }
        // A failing future is turned into a 500 by the central exception handler.
        return stationService.getStationsWithMeasurements(utcDateTime).thenApply(ResponseEntity::ok);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateMeetstation(@PathVariable long id, @RequestBody UpdateStationRequest updaterequest, @AuthenticationPrincipal User currentUser) throws Exception {
        Station station = stationService.GetStationById(id);
        if (!isOwnerOrAdmin(currentUser, station.getUserid())) {
            throw new AccessDeniedException("You are not allowed to edit this station");
        }
        station.setIs_public(updaterequest.is_public());
        if (updaterequest.name() != null && !updaterequest.name().isEmpty()) {
            station.setName(updaterequest.name());
        }
        stationService.editstation(id, station);
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @PutMapping("/transfer/{id}")
    public ResponseEntity<?> transferMeetstation(@PathVariable long id, @Valid @RequestBody TransferStationRequest request, @AuthenticationPrincipal User currentUser) {
        Station station = stationService.GetStationById(id);
        if (!isOwnerOrAdmin(currentUser, station.getUserid())) {
            throw new AccessDeniedException("You are not allowed to transfer this station");
        }
        stationService.transferOwnership(id, request.newOwnerEmail());
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    private boolean isOwnerOrAdmin(User currentUser, Long ownerId) {
        if (currentUser == null) {
            return false;
        }
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.SUPER_ADMIN;
        return isAdmin || currentUser.getUserId().equals(ownerId);
    }

    @GetMapping("/measurements/{id}")
    public ResponseEntity<byte[]> getMeetstationMeasurements(@PathVariable int id, @RequestParam String startDate, @RequestParam String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        Instant startInstant;
        Instant endInstant;
        try {
            startInstant = LocalDateTime.parse(startDate, formatter).atZone(ZoneId.systemDefault()).toInstant();
            endInstant = LocalDateTime.parse(endDate, formatter).atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Dates must be in dd-MM-yyyy HH:mm format");
        }

        if (startInstant.isAfter(endInstant)) {
            throw new InvalidArgumentException("Start date is after end date");
        }
        byte[] pdfBytes = measurementService.getMeasurementsAsPDF(id, startInstant, endInstant);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "measurements.pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/Availibility/{stationId}/{workshopCode}")
    public ResponseEntity<Integer> IsAvailable(@PathVariable Long stationId, @PathVariable Long workshopCode) {
        if (!stationService.IsAvailable(stationId)) {
            return ResponseEntity.status(HttpStatus.OK).body(402);
        } else if (!adminService.verifyWorkshopCode(workshopCode)) {
            return ResponseEntity.status(HttpStatus.OK).body(403);
        }
        return ResponseEntity.status(HttpStatus.OK).body(200);
    }

    @PutMapping("/Claim")
    public ResponseEntity<String> ClaimStation(@RequestBody StationDto stationDto, @AuthenticationPrincipal User currentUser) {
        // Bind the claim to the authenticated user, never to a client-supplied id.
        stationDto.userid = currentUser.getUserId();
        stationService.ClaimMeetstation(new Station(stationDto));
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addStation(@RequestBody Station stationRequest) {
        Station station = new Station();
        station.setName(stationRequest.getName());
        station.setDatabase_tag("MJS");
        station.setIs_public(false);
        station.setIsActive(false);
        station.setRegistrationCode(stationRequest.getRegistrationCode());
        station.setLocation_locationid(stationRequest.getLocation_locationid());

        Station savedStation = stationService.addStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStation);
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
