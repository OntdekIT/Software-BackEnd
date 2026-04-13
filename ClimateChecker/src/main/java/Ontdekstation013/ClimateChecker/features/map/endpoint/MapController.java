package Ontdekstation013.ClimateChecker.features.map.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080", "http://localhost:3000"}, allowCredentials = "true")
public class MapController {

    @GetMapping("/default-view")
    public ResponseEntity<MapConfigDto> getDefaultMapView() {

        double[] center = {51.5555, 5.0913};
        int zoom = 12;

        MapConfigDto config = new MapConfigDto(center, zoom);
        return ResponseEntity.ok(config);
    }
}
