package Ontdekstation013.ClimateChecker.features.station.endpoint;

import Ontdekstation013.ClimateChecker.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationMapper;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.station.endpoint.dto.AddStationRequest;
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

@Tag(name = "Meetstations", description = "Meetstations opvragen, claimen, bewerken en overdragen")
@RestController
@RequestMapping("/api/Meetstation")
public class StationController {
    private final StationService stationService;
    private final MeasurementService measurementService;
    private final WorkshopService adminService;

    public StationController(StationService stationService, MeasurementService measurementService, WorkshopService workshopService) {
        this.stationService = stationService;
        this.measurementService = measurementService;
        this.adminService = workshopService;
    }

    @Operation(summary = "Geef een meetstation op id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "404", description = "Niet gevonden")})
    @GetMapping("{id}")
    public ResponseEntity<StationDto> getMeetstation(@PathVariable Long id) {
        StationDto stationDto = id != 0 ? stationService.ReadById(id) : null;
        if (stationDto == null) {
            throw new NotFoundException("Station not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(stationDto);
    }

    @Operation(summary = "Geef alle meetstations, optioneel gefilterd")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer")})
    @GetMapping("/stations")
    public ResponseEntity<?> getAllMeetstations(GetAllStationsRequest request) {
        List<Station> stations = stationService.getAllStations(StationMapper.toStationFilter(request));
        List<StationDto> responses = new ArrayList<>();

        for (Station station : stations) {
            responses.add(StationMapper.toStationDTO(station));
        }

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Geef meetstations met hun meting op een tijdstip")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer")})
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

    @Operation(summary = "Pas naam en zichtbaarheid van een station aan (eigenaar of beheerder)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "401", description = "Niet ingelogd"), @ApiResponse(responseCode = "403", description = "Geen toegang")})
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

    @Operation(summary = "Draag een station over aan een andere gebruiker")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer"), @ApiResponse(responseCode = "401", description = "Niet ingelogd"), @ApiResponse(responseCode = "403", description = "Geen toegang")})
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

    @Operation(summary = "Download de metingen van een station als PDF")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer")})
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

    @Operation(summary = "Controleer of een station beschikbaar is en de workshopcode geldig is")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Statuscode in de body: 200 ok, 402 station bezet, 403 ongeldige workshopcode"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer")})
    @GetMapping("/Availibility/{stationId}/{workshopCode}")
    public ResponseEntity<Integer> IsAvailable(@PathVariable Long stationId, @PathVariable Long workshopCode) {
        if (!stationService.IsAvailable(stationId)) {
            return ResponseEntity.status(HttpStatus.OK).body(402);
        } else if (!adminService.verifyWorkshopCode(workshopCode)) {
            return ResponseEntity.status(HttpStatus.OK).body(403);
        }
        return ResponseEntity.status(HttpStatus.OK).body(200);
    }

    @Operation(summary = "Claim een station voor de ingelogde gebruiker")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer"), @ApiResponse(responseCode = "401", description = "Niet ingelogd")})
    @PutMapping("/Claim")
    public ResponseEntity<String> ClaimStation(@RequestBody StationDto stationDto, @AuthenticationPrincipal User currentUser) {
        // Bind the claim to the authenticated user, never to a client-supplied id.
        // A station that already has an owner is rejected with 409 (see StationService).
        stationDto.userid = currentUser.getUserId();
        stationService.ClaimMeetstation(new Station(stationDto));
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @Operation(summary = "Voeg een nieuw station toe")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Station aangemaakt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer"), @ApiResponse(responseCode = "401", description = "Niet ingelogd"), @ApiResponse(responseCode = "500", description = "Onverwachte fout")})
    @PostMapping("/add")
    public ResponseEntity<?> addStation(@Valid @RequestBody AddStationRequest request) {
        Station station = new Station();
        station.setStationid(request.stationid() != null ? request.stationid() : request.registrationCode());
        station.setName(request.name());
        station.setDatabase_tag("MJS");
        station.setIs_public(false);
        station.setIsActive(false);
        station.setRegistrationCode(request.registrationCode());
        station.setLocation_locationid(request.location_locationid());

        Station savedStation = stationService.addStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStation.toDto());
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
