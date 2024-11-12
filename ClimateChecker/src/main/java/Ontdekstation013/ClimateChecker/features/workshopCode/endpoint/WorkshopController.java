package Ontdekstation013.ClimateChecker.features.workshopCode.endpoint;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.workshopCode.Workshop;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopService;
import Ontdekstation013.ClimateChecker.features.workshopCode.endpoint.dto.WorkshopRequest;
import Ontdekstation013.ClimateChecker.features.workshopCode.endpoint.dto.WorkshopResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshopcodes")
public class WorkshopController {
    private final WorkshopService workshopService;
    private final UserService userService;

    public WorkshopController(WorkshopService workshopService, UserService userService) {
        this.workshopService = workshopService;
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<WorkshopResponse> createNewWorkshop(@RequestBody WorkshopRequest request) {
        throw new UnsupportedOperationException();
    }

    @GetMapping()
    public ResponseEntity<List<Workshop>> getAllActiveWorkshops() {
        throw new UnsupportedOperationException();
    }

    @GetMapping()
    public ResponseEntity<List<Workshop>> getAllExpiredWorkshops() {
        throw new UnsupportedOperationException();
    }

    // TODO: Combine workshop with userService
    @GetMapping()
    public ResponseEntity<List<User>> getAllUsersByWorkshopCode() {
        throw new UnsupportedOperationException();
    }
}
