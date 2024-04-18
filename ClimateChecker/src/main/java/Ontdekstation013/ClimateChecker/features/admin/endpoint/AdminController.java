package Ontdekstation013.ClimateChecker.features.admin.endpoint;

import Ontdekstation013.ClimateChecker.features.admin.AdminService;
import Ontdekstation013.ClimateChecker.features.admin.WorkshopCode;
import Ontdekstation013.ClimateChecker.features.admin.WorkshopCodeRepository;


import Ontdekstation013.ClimateChecker.features.admin.wsCodeRequest;
import Ontdekstation013.ClimateChecker.features.authentication.JWTService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/Admin")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class AdminController {
    private final AdminService adminService;
    private final JWTService jwtService;

    @Autowired
    public AdminController(AdminService adminService, JWTService jwtService)
    {
        this.adminService = adminService;
        this.jwtService = jwtService;
    }

//    @GetMapping("createworkshopcode")
//    @CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
//    public ResponseEntity<Void> createNewWorkshopCode(@RequestParam Long duration, @RequestParam Long length) {
//        WorkshopCode workshopCode = adminService.createWorkshopCode(duration, length);
//        if (workshopCode != null) {
//            return ResponseEntity.status(HttpStatus.OK).body(null);
//        }
//        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
//    }

    @PostMapping("createworkshopcode")
    public ResponseEntity<Void> createNewWorkshopCode(@RequestBody wsCodeRequest request) {
        WorkshopCode workshopCode = adminService.createWorkshopCode(request.getDuration(), request.getLength());
        if (workshopCode != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

}
