package Ontdekstation013.ClimateChecker.features.workshopCode.endpoint;

import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.features.workshopCode.Workshop;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopMapper;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopService;
import Ontdekstation013.ClimateChecker.features.workshopCode.endpoint.dto.WorkshopRequest;
import Ontdekstation013.ClimateChecker.features.workshopCode.endpoint.dto.WorkshopResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workshopcodes")
public class WorkshopController {
    private final WorkshopService workshopService;
//    private final UserService userService;

    public WorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @PostMapping()
    public ResponseEntity<WorkshopResponse> createNewWorkshop(@RequestBody WorkshopRequest request) {
        Workshop workshop = workshopService.createWorkshop(LocalDateTime.parse(request.expirationDate(), DateTimeFormatter.ISO_DATE_TIME));
        WorkshopResponse response = WorkshopMapper.toResponse(workshop);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

    // TODO: Combine workshop with userService
    @GetMapping("/{code}")
    public ResponseEntity<List<UserResponse>> getAllUsersByWorkshopCode(@PathVariable long code) {
        throw new UnsupportedOperationException();
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<?> deleteWorkshopCode(@PathVariable long code) {
        workshopService.deleteWorkshopCode(code);
        return ResponseEntity.noContent().build();
    }
}
