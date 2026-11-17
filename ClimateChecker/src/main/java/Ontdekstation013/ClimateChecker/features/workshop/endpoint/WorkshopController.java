package Ontdekstation013.ClimateChecker.features.workshop.endpoint;

import Ontdekstation013.ClimateChecker.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopMapper;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopService;
import Ontdekstation013.ClimateChecker.features.workshop.endpoint.dto.WorkshopRequest;
import Ontdekstation013.ClimateChecker.features.workshop.endpoint.dto.WorkshopResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Workshops", description = "Workshopcodes beheren (alleen beheerders)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {
    private final WorkshopService workshopService;
    private final UserService userService;

    public WorkshopController(WorkshopService workshopService, UserService userService) {
        this.workshopService = workshopService;
        this.userService = userService;
    }

    @Operation(summary = "Maak een workshop met een vervaldatum")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Workshop aangemaakt"), @ApiResponse(responseCode = "403", description = "Geen toegang")})
    @PostMapping()
    public ResponseEntity<WorkshopResponse> createNewWorkshop(@RequestBody WorkshopRequest request) {
        Workshop workshop = workshopService.createWorkshop(LocalDateTime.parse(request.expirationDate(), DateTimeFormatter.ISO_DATE_TIME));
        WorkshopResponse response = WorkshopMapper.toResponse(workshop);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Geef actieve of verlopen workshops")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "403", description = "Geen toegang")})
    @GetMapping()
    public ResponseEntity<List<WorkshopResponse>> getAllWorkshops(@RequestParam(defaultValue = "false") boolean isExpired) {
        List<Workshop> workshops;
        if (isExpired) {
            workshops = workshopService.getAllExpiredWorkshops();
        } else {
            workshops = workshopService.getAllActiveWorkshops();
        }
        List<WorkshopResponse> response = workshops.stream()
                .map(WorkshopMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Geef de gebruikers die via een workshopcode zijn geregistreerd")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "403", description = "Geen toegang")})
    @GetMapping("/{code}/users")
    public ResponseEntity<List<UserResponse>> getAllUsersByWorkshopCode(@PathVariable long code) {
        Workshop workshop = workshopService.getByCode(code);
        List<User> users = userService.getUsersByWorkshop(workshop);
        List<UserResponse> response = new ArrayList<>();

        for(User user : users) {
            response.add(UserMapper.toUserResponse(user, false));
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verwijder een workshopcode")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Verwijderd"), @ApiResponse(responseCode = "403", description = "Geen toegang")})
    @DeleteMapping("/{code}")
    public ResponseEntity<?> deleteWorkshopCode(@PathVariable long code) {
        workshopService.deleteWorkshopCode(code);
        return ResponseEntity.noContent().build();
    }
}
