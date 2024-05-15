package Ontdekstation013.ClimateChecker.features.meetstation.endpoint;

import Ontdekstation013.ClimateChecker.features.meetstation.MeetstationService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/Meetstation")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class MeetstationController {
    private final MeetstationService meetstationService;

    public MeetstationController (MeetstationService meetstationService){
        this.meetstationService = meetstationService;
    }

    @GetMapping("{id}")
    public ResponseEntity<MeetstationDto> getMeetstation(@PathVariable Long id) throws Exception {
        try{
            if (id != 0){
                MeetstationDto meetstationDto = meetstationService.ReadById(id);
                return ResponseEntity.status(HttpStatus.OK).body(meetstationDto);
            }
        }
        catch (Exception error){

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
