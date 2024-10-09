package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StationService {
    private final IStationRepository stationRepository;
    public StationService(IStationRepository stationRepository){
        this.stationRepository = stationRepository;
    }

    public StationDto ReadById(Long id){
        Optional<Station> meetstation = Optional.ofNullable(stationRepository.getMeetstationByStationid(id));
        if(meetstation.isPresent())
        {
            return meetstation.get().toDto();
        }
        return null;
    }

    public void UpdateMeetstation(Station station){
        stationRepository.save(station);
    }

    public void ClaimMeetstation(Station station){
        Station existingStation = stationRepository.getMeetstationByStationid(station.getStationid());
        existingStation.setUserid(station.getUserid());
        existingStation.setName(station.getName());
        existingStation.setDatabase_tag(station.getDatabase_tag());
        existingStation.setIs_public(station.getIs_public());
        existingStation.setRegistrationCode(station.getStationid());
        stationRepository.save(existingStation);
    }

    public Boolean IsAvailable(Long stationId){
        Optional<Station> optionalMeetstation = stationRepository.findById(stationId);
        if (optionalMeetstation.isPresent()) {
            Station station = optionalMeetstation.get();
            return station.getUserid() == null;
        }
        return false;
    }
}
