package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class StationService {
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    public StationService(StationRepository stationRepository, UserRepository userRepository) {
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
    }

    public StationDto ReadById(Long id) {
        Optional<Station> meetstation = Optional.ofNullable(stationRepository.getMeetstationByStationid(id));
        if (meetstation.isPresent()) {
            return meetstation.get().toDto();
        }
        return null;
    }

    public Station getByRegistrationCode(long registrationCode) {
        return stationRepository.getByRegistrationCode(registrationCode);
    }

    public void UpdateMeetstation(Station station) {
        stationRepository.save(station);
    }

    public void ClaimMeetstation(Station station) {
        Station existingStation = stationRepository.getMeetstationByStationid(station.getStationid());
        existingStation.setUserid(station.getUserid());
        existingStation.setName(station.getName());
        existingStation.setDatabase_tag(station.getDatabase_tag());
        existingStation.setIs_public(station.getIs_public());
        existingStation.setRegistrationCode(station.getStationid());
        stationRepository.save(existingStation);
    }

    public Boolean IsAvailable(Long stationId) {
        Optional<Station> optionalMeetstation = stationRepository.findById(stationId);
        if (optionalMeetstation.isPresent()) {
            Station station = optionalMeetstation.get();
            return station.getUserid() == null;
        }
        return false;
    }

    public Station GetStationById(long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

    }

    public List<Station> getAllStations(StationFilter filter) {
        if (filter.getUsername() != null && !filter.getUsername().isEmpty()) {
            List<User> users = userRepository.findByFullName(filter.getUsername());
            if (users.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> userIds = users.stream().map(User::getUserId).collect(Collectors.toList());
            filter.setUserIds(userIds);  // Modify filter to support multiple userIds
        }
        return stationRepository.findStationsByOptionalFilters(filter.getName(), filter.getDatabaseTag(), filter.getIsPublic(), filter.getRegistrationCode(), filter.getUserIds(), filter.getIsActive());
    }


    public void editstation(long id, Station newstation) {
        Optional<Station> getStationResult = stationRepository.findById(id);

        if (getStationResult.isPresent()) {
            Station stationToUpdate = getStationResult.get();
            stationToUpdate.setIs_public(newstation.getIs_public());
            stationToUpdate.setName(newstation.getName());
            stationRepository.save(stationToUpdate);
        } else {
            throw new NotFoundException("Station not found");
        }
    }

    public List<Station> findAll(){
        return stationRepository.findAll();
    }
}
