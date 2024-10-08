package Ontdekstation013.ClimateChecker.features.workshopCode.endpoint;

import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCode;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeRequest;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshopcodes")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class WorkshopCodeController {
    private final WorkshopCodeService workshopCodeService;

    public WorkshopCodeController(WorkshopCodeService workshopCodeService) {
        this.workshopCodeService = workshopCodeService;
    }

    @PostMapping("/")
    public ResponseEntity<?> createNewWorkshopCode(@RequestBody WorkshopCodeRequest request) {
        WorkshopCode savedCode = workshopCodeService.createWorkshopCode(request.expirationDate());
        return new ResponseEntity<>(savedCode, HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<WorkshopCode>> getAllWorkshopCodes() {
        List<WorkshopCode> workshopCodeList = workshopCodeService.getAllWorkshopCodes();
        return new ResponseEntity<>(workshopCodeList, HttpStatus.OK);
    }
}
