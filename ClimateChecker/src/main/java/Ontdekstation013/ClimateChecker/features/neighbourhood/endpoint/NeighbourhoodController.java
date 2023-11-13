package Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint;

import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/neighbourhood")
@RequiredArgsConstructor
public class NeighbourhoodController {
    private final NeighbourhoodService neighbourhoodService;

    @GetMapping("/all")
    public List<NeighbourhoodDTO> getNeighbourhoodData() {
        return neighbourhoodService.getNeighbourhoodData();
    }
}