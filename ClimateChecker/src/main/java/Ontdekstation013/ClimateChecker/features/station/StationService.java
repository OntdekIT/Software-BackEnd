package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StationService {
    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final MeasurementService measurementService;
    public StationService(StationRepository stationRepository, UserRepository userRepository, MeasurementService measurementService) {
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
        this.measurementService = measurementService;
    }

    public Station addStation(Station station) {
        return stationRepository.save(station);
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
            filter.setUserIds(userIds);
        }
        return stationRepository.findStationsByOptionalFilters(filter.getName(), filter.getDatabaseTag(), filter.getIsPublic(), filter.getRegistrationCode(), filter.getUserIds(), filter.getIsActive());
    }

    public List<StationDto> getStationsWithMeasurements(Instant timestamp) {
        List<Station> stations = stationRepository.findAll();
        List<MeasurementDto> measurements = measurementService.getMeasurementsAtTime(timestamp);

        Map<Long, StationDto> stationMap = new HashMap<>();

        // Maak DTOs van Stations uit database
        for (Station station : stations) {
            stationMap.put(station.getStationid(), StationMapper.toStationDTOWithMeasurement(station, new ArrayList<>()));
        }

        // Informatie van measurement geven aan Stations
        for (MeasurementDto measurement : measurements) {
            Long stationId = Long.valueOf(measurement.getId());

            if(stationMap.containsKey(stationId)) {
                // Geef stations in onze database longitude en latitude
                StationDto existingStation = stationMap.get(stationId);

                // Extra check om te kijken of de station al geen latitude en longitude heeft
                if (existingStation.latitude == 0 && existingStation.longitude == 0) {
                    existingStation.latitude = measurement.getLatitude();
                    existingStation.longitude = measurement.getLongitude();
                }
            }
            if (!stationMap.containsKey(stationId)) {
                // Maak nieuwe stationDTO als er de station niet in onze database zit
                StationDto newStation = new StationDto(
                        stationId,
                        null,
                        "Unknown",
                        true,
                        null,
                        null,
                        measurement.getLatitude(),
                        measurement.getLongitude(),
                        null,
                        true,
                        new ArrayList<>()
                );
                stationMap.put(stationId, newStation);
            }

            // Voeg measurement toe aan station waar die bij hoort (zelfde ID)
            stationMap.get(stationId).measurementDtoList.add(measurement);
        }

        return new ArrayList<>(stationMap.values());
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
