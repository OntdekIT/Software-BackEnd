package Ontdekstation013.ClimateChecker.Services;

import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.Sensor;
import Ontdekstation013.ClimateChecker.models.Station;

import java.util.ArrayList;
import java.util.List;

import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.repositories.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import Ontdekstation013.ClimateChecker.Services.converters.*;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public final SensorService sensorService;
    private final StationConverter stationConverter;

    @Autowired
    public StationService(StationRepository stationRepository, SensorService sensorService) {
        this.stationRepository = stationRepository;
        this.sensorService = sensorService;
        this.stationConverter = new StationConverter(sensorService);
    }

    public stationDto findStationById(long id) {
        Station station = stationRepository.findById(id).get();

        return stationConverter.stationToStationDTO(station);
    }

    public List<stationTitleDto> getAllByUserId(long userId) {
        Iterable<Station> stationList = stationRepository.findAllByUserId(userId);

        List<stationTitleDto> newDtoList = new ArrayList<>();
        for (Station station: stationList
        ) {

            newDtoList.add(stationConverter.stationToStationTitleDTo(station));

        }

        return newDtoList;
    }

    // not yet functional
    public List<stationTitleDto> getAll() {
        Iterable<Station> StationList = stationRepository.findAll();
        List<stationTitleDto> newDtoList = new ArrayList<>();
        for (Station station: StationList
        ) {

            newDtoList.add(stationConverter.stationToStationTitleDTo(station));

        }


        return newDtoList;
    }

    public List<stationDto> getAllStations() {
        Iterable<Station> StationList = stationRepository.findAll();
        List<stationDto> newDtoList = new ArrayList<>();
        for (Station station: StationList)
        {
            newDtoList.add(stationConverter.stationToStationDTO(station));
        }
        return newDtoList;
    }

    // not yet functional
    public List<stationTitleDto> getAllByPageId(long pageId) {
        List<stationTitleDto> newDtoList = new ArrayList<stationTitleDto>();


        return newDtoList;
    }


    // Returns false if not all information is filled in
    // Returns true if successful
    public boolean createStation(registerStationDto stationDto) {

        if (stationDto.getUserId() < 1 || stationDto.getStationname().equals("")
                || stationDto.getAddress().equals("")) {
            return false;
        }


        User owner = new User();
        owner.setUserID(stationDto.getUserId());

        Location location = new Location(stationDto.getAddress(), stationDto.getLatitude(), stationDto.getLongitude());

        Station station = new Station(owner, stationDto.getStationname(), stationDto.getHeight(), location, stationDto.isIspublic());

        stationRepository.save(station);

        return true;
    }

    public void deleteStation(long id) {

        stationRepository.deleteById(id);
    }

    public void editStation(editStationDto stationDto) {
        Station currentStation = stationRepository.findById(stationDto.getId()).get();

        currentStation.setName(stationDto.getName());
        currentStation.setHeight(stationDto.getHeight());
        currentStation.setPublic(stationDto.isIspublic());

        Location location = new Location(stationDto.getAddress(), stationDto.getLatitude(), stationDto.getLongitude());
        currentStation.setLocation(location);

        stationRepository.save(currentStation);
    }
}