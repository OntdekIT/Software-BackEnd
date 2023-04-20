package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.dto.heatmapPointDto;
import Ontdekstation013.ClimateChecker.models.dto.sensorDto;
import Ontdekstation013.ClimateChecker.models.dto.stationDto;
import Ontdekstation013.ClimateChecker.services.LocationService;
import Ontdekstation013.ClimateChecker.services.SensorService;
import Ontdekstation013.ClimateChecker.services.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/Heatmap")
public class HeatmapController {

    private final SensorService sensorService;
    private final StationService stationService;
    private final LocationService locationService;

    @Autowired
    public HeatmapController(SensorService sensorService, StationService stationService, LocationService locationService )
    {
        this.sensorService = sensorService;
        this.stationService = stationService;
        this.locationService = locationService;
    }

    @GetMapping("{typeId}")
    public ResponseEntity<List<heatmapPointDto>> getDataBySensorType(@PathVariable long typeId)
    {
        try
        {
            List<sensorDto> sensorData = sensorService.getSensorsByType(typeId);

            if(sensorData.size() == 0) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            ArrayList<heatmapPointDto> returnValue = new ArrayList<heatmapPointDto>();

            for(sensorDto dto : sensorData)
            {
                stationDto station = stationService.findStationById(dto.getStationId());
                Location location = locationService.findLocationById(station.getLocationId());

                returnValue.add(new heatmapPointDto(location.getLongitude(), location.getLatitude(), dto.getData()));
            }

            return ResponseEntity.status(HttpStatus.OK).body(returnValue);
        }
        catch(Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }


    }


}
