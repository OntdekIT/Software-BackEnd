package Ontdekstation013.ClimateChecker.features.admin.endpoint;

import Ontdekstation013.ClimateChecker.features.admin.AdminService;
import Ontdekstation013.ClimateChecker.features.admin.WorkshopCode;

import Ontdekstation013.ClimateChecker.features.admin.wsCodeRequest;

import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDto;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/Admin")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;

    @Autowired
    public AdminController(AdminService adminService, UserService userService)
    {
        this.adminService = adminService;
        this.userService = userService;
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

    @GetMapping("getworkshopcodes")
    public ResponseEntity<List<WorkshopCode>> getWorkshopCode() {
        List<WorkshopCode> workshopCodeList = adminService.getWorkshopCodes();
        return ResponseEntity.status(200).body(workshopCodeList);
    }

    @PostMapping("grantuseradmin")
    public ResponseEntity<String> grantUserAdmin(@RequestBody GrantUserAdminRequestDto request) {
        if (request.getUserId() != null & request.getAdminRights() != null) {
            Long userId = Long.parseLong(request.getUserId());
            Boolean adminRights = request.getAdminRights();
            userDto dto = userService.findUserById(userId);

            if(dto != null)
            {
                dto.setAdmin(adminRights);
                Long returnedUserId = userService.grantUserAdmin(dto);
                return ResponseEntity.status(200).body(returnedUserId.toString());
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User could not be found");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please fill out all fields");
    }

}
