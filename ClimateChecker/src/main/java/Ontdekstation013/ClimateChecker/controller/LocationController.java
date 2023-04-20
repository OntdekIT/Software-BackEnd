package Ontdekstation013.ClimateChecker.controller;


import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.dto.createLocationDto;
import Ontdekstation013.ClimateChecker.models.dto.editLocationDto;
import Ontdekstation013.ClimateChecker.Services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/Locations")
public class LocationController {

    final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService){
        this.locationService = locationService;
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<editLocationDto> getLocation(@PathVariable int locationId){
        Location location = locationService.findLocationById(locationId);

        editLocationDto editLocationDto = new editLocationDto();
        editLocationDto.setDirection(location.getDirection());
        editLocationDto.setHeight(location.getHeight());
        editLocationDto.setOutside(location.isOutside());

        return ResponseEntity.ok(editLocationDto);
    }

    @PutMapping()
    public ResponseEntity<String> editLocation(@RequestBody editLocationDto editLocationDto){
        boolean succes = locationService.editLocation(editLocationDto);

        if(succes){
            return new ResponseEntity<>("Location updated", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Location not updated", HttpStatus.NOT_ACCEPTABLE);
    }


    @PostMapping()
    public ResponseEntity<String> createLocation(@RequestBody createLocationDto createLocationDto){
        long locationId = locationService.createLocation(createLocationDto);

        if(locationId > 0){
            return new ResponseEntity<>("Location added", HttpStatus.CREATED);

        }
        return new ResponseEntity<>("Location not added", HttpStatus.BAD_REQUEST);
    }

}
