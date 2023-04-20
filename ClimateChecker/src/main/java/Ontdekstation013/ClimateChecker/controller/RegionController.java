package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.Region;
import Ontdekstation013.ClimateChecker.models.dto.RegionInfoDto;
import Ontdekstation013.ClimateChecker.Services.RegionCordsService;
import Ontdekstation013.ClimateChecker.Services.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/Region")
public class RegionController {

    private final RegionService regionService;
    private final RegionCordsService regionCordsService;

    @Autowired
    public RegionController(RegionService regionService, RegionCordsService regionCordsService)
    {
        this.regionService = regionService;
        this.regionCordsService = regionCordsService;
    }

    @GetMapping
    public ResponseEntity<List<Region>> getAll()
    {
        try
        {
            List<Region> regions = regionService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(regions);
        }
        catch(Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/regioninfo")
    public ResponseEntity<List<RegionInfoDto>> getAllRegionCords()
    {
        try
        {
            List<RegionInfoDto> regionInfoList = regionCordsService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(regionInfoList);
        }
        catch(Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
