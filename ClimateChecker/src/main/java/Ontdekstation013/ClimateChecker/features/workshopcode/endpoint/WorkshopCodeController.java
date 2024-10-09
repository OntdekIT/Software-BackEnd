package Ontdekstation013.ClimateChecker.features.workshopcode.endpoint;

import Ontdekstation013.ClimateChecker.features.workshopcode.WorkshopCode;
import Ontdekstation013.ClimateChecker.features.workshopcode.WorkshopCodeRequest;
import Ontdekstation013.ClimateChecker.features.workshopcode.WorkshopCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshopcodes")
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
