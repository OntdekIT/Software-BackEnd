package Ontdekstation013.ClimateChecker.features.admin.endpoint;

import Ontdekstation013.ClimateChecker.features.admin.AdminService;
import Ontdekstation013.ClimateChecker.features.admin.WorkshopCode;

import Ontdekstation013.ClimateChecker.features.admin.wsCodeRequest;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/Admin")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class AdminController {
    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService)
    {
        this.adminService = adminService;
    }

    @PostMapping("createworkshopcode")
    public ResponseEntity<String> createNewWorkshopCode(@RequestBody wsCodeRequest request) {
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

}