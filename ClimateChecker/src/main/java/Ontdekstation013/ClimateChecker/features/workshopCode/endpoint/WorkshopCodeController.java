package Ontdekstation013.ClimateChecker.features.workshopCode.endpoint;

import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeService;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCode;

import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeRequest;

import Ontdekstation013.ClimateChecker.features.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Admin")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class WorkshopCodeController {
    private final WorkshopCodeService adminService;
    private final UserService userService;

    @Autowired
    public WorkshopCodeController(WorkshopCodeService adminService, UserService userService)
    {
        this.adminService = adminService;
        this.userService = userService;
    }

    @PostMapping("createWorkshopCode")
    public ResponseEntity<String> createNewWorkshopCode(@RequestBody WorkshopCodeRequest request) {
        WorkshopCode workshopCode = adminService.createWorkshopCode(request.getDuration(), request.getLength());
        if (workshopCode != null) {
            Long code = workshopCode.getCode();
            if (adminService.VerifyWorkshopCode(code)) {
                return ResponseEntity.status(200).body(code.toString());
            }
            else {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to verify, code has expired");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create workshop code");
    }

    @GetMapping("getworkshopcodes")
    public ResponseEntity<List<WorkshopCode>> getWorkshopCode() {
        List<WorkshopCode> workshopCodeList = adminService.getWorkshopCodes();
        return ResponseEntity.status(200).body(workshopCodeList);
    }



}
