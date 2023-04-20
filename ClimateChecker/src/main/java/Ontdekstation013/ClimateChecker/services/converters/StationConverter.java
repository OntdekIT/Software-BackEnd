package Ontdekstation013.ClimateChecker.services.converters;

import Ontdekstation013.ClimateChecker.models.Station;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.services.SensorService;
import org.springframework.stereotype.Service;

@Service
public class StationConverter {
    private final SensorService sensorService;

    public StationConverter(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    public stationTitleDto stationToStationTitleDTo(Station station){
        stationTitleDto newdto = new stationTitleDto();
        newdto.setId(station.getStationID());
        newdto.setName(station.getName());

        return newdto;
    };

    public stationDto stationToStationDTO(Station station){
        stationDto newdto = new stationDto();
        newdto.setId(station.getStationID());
        newdto.setName(station.getName());
        newdto.setLocationId(station.getLocation().getLocationID());
        newdto.setLatitude(station.getLocation().getLatitude());
        newdto.setLongitude(station.getLocation().getLongitude());
        newdto.setIspublic(station.isPublic());
        newdto.setSensors(sensorService.getSensorsByStation(station.getStationID()));

        return newdto;
    }

}
