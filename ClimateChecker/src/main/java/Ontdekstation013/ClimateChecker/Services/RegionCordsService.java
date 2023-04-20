package Ontdekstation013.ClimateChecker.Services;

import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.RegionCords;
import Ontdekstation013.ClimateChecker.models.SensorType;
import Ontdekstation013.ClimateChecker.models.dto.RegionAverageDto;
import Ontdekstation013.ClimateChecker.models.dto.RegionInfoDto;
import Ontdekstation013.ClimateChecker.models.dto.sensorDto;
import Ontdekstation013.ClimateChecker.models.dto.stationDto;
import Ontdekstation013.ClimateChecker.models.logic.RegionSurface;
import Ontdekstation013.ClimateChecker.repositories.RegionCordsRepository;
import Ontdekstation013.ClimateChecker.repositories.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegionCordsService {

    private final RegionCordsRepository regionCordsRepository;
    private final RegionService regionService;
    private final LocationService locationService;
    private final StationService stationService;
    private final TypeRepository typeRepository;

    public List<RegionInfoDto> getAll()
    {
        List<RegionCords> cords = regionCordsRepository.findAll();
        List<RegionInfoDto> dtos = new ArrayList<>();

        //Used for splitting data into Regions. (RegionCords contains multiple lines of cords per region).
        long currentID = 0;

        for(RegionCords cord : cords)
        {
            if(currentID != cord.getRegion().getId())
            {
                RegionInfoDto regionInfoDto = new RegionInfoDto(cord.getRegion(), cords);
                regionInfoDto.averageData = getAverageRegionData(regionInfoDto);

                dtos.add(regionInfoDto);
                currentID = cord.getRegion().getId();
            }
        }

        return dtos;
    }

    public List<stationDto> getStationsInRegion(RegionInfoDto info)
    {
        List<stationDto> stations = stationService.getAllStations();
        List<stationDto> stationInRegion = new ArrayList<stationDto>();

        RegionSurface surface = new RegionSurface(info.cordsList);

        for(stationDto station : stations)
        {
            Location location = locationService.findLocationById(station.getLocationId());

            if(surface.contains(location.getLatitude(), location.getLongitude()))
            {
                stationInRegion.add(station);
            }
        }

        return stationInRegion;
    }


    public List<RegionAverageDto> getAverageRegionData(RegionInfoDto info)
    {
        List<RegionAverageDto> returnvalue = new ArrayList<>();

        List<stationDto> stationInRegion = getStationsInRegion(info);

        Iterable<SensorType> sensorTypes = typeRepository.findAll();

        for(SensorType type : sensorTypes)
        {
            double avgData = 0;
            double count = 0;

            for(stationDto station : stationInRegion)
            {
                for(sensorDto sensor : station.getSensors())
                {
                    if(sensor.getTypeId() == type.getTypeID())             
                    {
                        avgData+= sensor.getData();
                        count++;
                    }
                }
            }

            if(count > 0)
            {
                avgData = SensorService.avgformat(avgData/count);

                returnvalue.add(new RegionAverageDto((int)type.getTypeID(), type.getTypeName(), avgData));
            }
        }

        return returnvalue;
    }

    @Autowired
    public RegionCordsService(RegionCordsRepository regionCordsRepository, RegionService regionService, StationService stationService
    , LocationService locationService, TypeRepository typeRepository)
    {
        this.regionCordsRepository = regionCordsRepository;
        this.regionService = regionService;
        this.stationService = stationService;
        this.locationService = locationService;
        this.typeRepository = typeRepository;
    }
}
