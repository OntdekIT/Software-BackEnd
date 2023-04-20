package Ontdekstation013.ClimateChecker.Services;

import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.Station;

import java.util.ArrayList;
import java.util.List;

import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.repositories.StationRepository;
import Ontdekstation013.ClimateChecker.repositories.StationRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StationService {

    private final StationRepositoryCustom stationRepository;
    private final SensorService sensorService;


    @Autowired
    public StationService(StationRepositoryCustom stationRepository, SensorService sensorService) {
        this.stationRepository = stationRepository;
        this.sensorService = sensorService;
    }

    public stationDto findStationById(long id) {
        Station station = stationRepository.findById(id).get();
        stationDto newdto = stationToStationDTO(station);
        return newdto;
    }

    public stationDto findStationByRegistrationCode(long registrationCode, String databaseTag) {
        List<Station> stations = stationRepository.findAllByRegistrationCodeAndDatabaseTag(registrationCode, databaseTag);
        stationDto newdto = null;
        if(stations.size() > 0){
            newdto = stationToStationDTO(stations.get(0));
        }
        return newdto;
    }

    public stationTitleDto stationToStationTitleDTo (Station station){
        stationTitleDto newdto = new stationTitleDto();
        newdto.setId(station.getStationID());
        newdto.setName(station.getName());


        return newdto;
    }


    public stationDto stationToStationDTO (Station station){
        stationDto newdto = new stationDto();
        newdto.setId(station.getStationID());
        newdto.setName(station.getName());
        newdto.setHeight(station.getLocation().getHeight());
        newdto.setDirection(station.getLocation().getDirection());
        newdto.setLocationId(station.getLocation().getLocationID());
        newdto.setLatitude(station.getLocation().getLatitude());
        newdto.setLongitude(station.getLocation().getLongitude());
        newdto.setIspublic(station.isPublic());
        newdto.setIsoutside(station.getLocation().isOutside());
        newdto.setSensors(sensorService.getSensorsByStation(station.getStationID()));

        return newdto;
    }

    public List<stationTitleDto> getAllByUserId(long userId) {
        Iterable<Station> stationList = stationRepository.findAllByOwner_UserID(userId);

        List<stationTitleDto> newDtoList = new ArrayList<>();
        for (Station station: stationList
        ) {

            newDtoList.add(stationToStationTitleDTo(station));

        }

        return newDtoList;
    }

    // not yet functional
    public List<stationTitleDto> getAll() {
        Iterable<Station> StationList = stationRepository.findAll();
        List<stationTitleDto> newDtoList = new ArrayList<>();
        for (Station station: StationList
        ) {

            newDtoList.add(stationToStationTitleDTo(station));

        }


        return newDtoList;
    }

    public List<stationDto> getAllStations() {
        Iterable<Station> StationList = stationRepository.findAll();
        List<stationDto> newDtoList = new ArrayList<>();
        for (Station station: StationList)
        {
            newDtoList.add(stationToStationDTO(station));
        }
        return newDtoList;
    }

    // not yet functional
    public List<stationTitleDto> getAllByPageId(long pageId) {
        List<stationTitleDto> newDtoList = new ArrayList<stationTitleDto>();


        return newDtoList;
    }


    // Koppel een gebruiker aan bestaan station
    public boolean registerStation(registerStationDto stationDto) {
        User owner = new User();
        owner.setUserID(stationDto.getUserId());

        Location location = new Location(); //stationDto.getHeight()
        location.setLocationID(3);

        Station station = new Station();

        stationRepository.save(station);

        return true;
    }

    public void deleteStation(long id) {

        stationRepository.deleteById(id);
    }

    public void editStation(editStationDto stationDto) {
        Station currentStation = stationRepository.findById(stationDto.getId()).get();
        currentStation.setName(stationDto.getName());
        currentStation.setPublic(stationDto.isPublic());

        stationRepository.save(currentStation);
    }

    public List<Station> findByRegistrationCode(long registrationCode, String databaseTag){
        return stationRepository.findAllByRegistrationCodeAndDatabaseTag(registrationCode, databaseTag);
    }



    // Zet meetjestad station in de database
    public boolean createStation(createStationDto createStationDto){
        boolean succes = false;

        Location location = new Location();
        location.setLocationID(createStationDto.getLocationId());
        Station station = new Station();
        station.setRegistrationCode((createStationDto.registrationCode));
        station.setLocation(location);
        Station check = stationRepository.save(station);
        if (check != null){
            succes = true;
        }

        return succes;
    }







}