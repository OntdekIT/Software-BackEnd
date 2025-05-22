package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
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
                        new ArrayList<>(),
                        null,
                        null,
                        null,
                        null
                );
                stationMap.put(stationId, newStation);
            }

            // Voeg measurement toe aan station waar die bij hoort (zelfde ID)
            stationMap.get(stationId).measurementDtoList.add(measurement);
        }

        return new ArrayList<>(stationMap.values());
    }

    public void UpdateStationsInDatabase(Instant timestamp) {
        //Haal measurements op
        List<MeasurementDto> measurements = measurementService.GetDailyMeasurements(timestamp);
        //Groepeer measurements op ID
        Map<Long, List<MeasurementDto>> measurementsById = measurements.stream().collect(Collectors.groupingBy(m -> Long.valueOf(m.getId())));
        //Maak lijst aan uniekeIds van alle measurements
        List<Long> uniqueIds = measurements.stream().map(MeasurementDto::getId).map(Long::valueOf).distinct().toList();
        //Haal alle stations uit database op en maak er DTOs van
        List<StationDto> stationsDB = new ArrayList<>();
        for (Station station : stationRepository.findAllById(uniqueIds))
        {
            stationsDB.add(StationMapper.toStationDTO(station));
        }
        //Haal alle Ids op van stations die al bestaan
        Set<Long> existingStationIds = stationsDB.stream().map(stationDto -> stationDto.stationid).collect(Collectors.toSet());
        //Maak stationDTOs voor elke unieke measurement ID die nog geen stationDTO heeft en voeg toe aan lijst alle stationDTOs
        uniqueIds.stream()
                .filter(id -> !existingStationIds.contains(id))
                .map(id -> new StationDto(
                        id,
                        null,
                        "MJS",
                        false,
                        0L,
                        null,
                        0f, // tijdelijke dummy coords
                        0f, // tijdelijke dummy coords
                        null,
                        null,
                        new ArrayList<>(),
                        false,
                        false,
                        false,
                        false
                ))
                .forEach(stationsDB::add);

        for(StationDto station : stationsDB)
        {
            //Zorg dat measurementDtoList nooit null is
            if (station.measurementDtoList == null) {
                station.measurementDtoList = new ArrayList<>();
            }

            //Haal alleen de relevante measurements op uit de map door de stationid als key te gebruiken
            List<MeasurementDto> relatedMeasurements = measurementsById.getOrDefault(station.stationid, List.of());

            for (MeasurementDto measurementDto : relatedMeasurements) {
                //Check of dat de station de measurement al heeft
                boolean alreadyExists = station.measurementDtoList.stream().anyMatch(m -> m.getId() == measurementDto.getId());

                if (!alreadyExists) {
                    station.measurementDtoList.add(measurementDto);
                }
            }
        }

        List<Station> stationEntities = stationsDB.stream()
                .map(dto -> {
                    Station station = new Station();
                    station.setStationid(dto.stationid);
                    station.setName(dto.name);
                    station.setDatabase_tag(dto.database_tag);
                    station.setIs_public(dto.is_public);
                    station.setRegistrationCode(dto.registrationCode);
                    station.setLocation_locationid(dto.location_locationid);
                    station.setUserid(dto.userid);
                    station.setIsActive(dto.isActive);
                    station.setTempError(dto.tempError);
                    station.setHumError(dto.humError);
                    station.setStofError(dto.stofError);
                    station.setLocError(dto.locError);

                    List<Measurement> measurementsEntityList = new ArrayList<>();

                    if (dto.measurementDtoList != null) {
                        for (MeasurementDto measurementDto : dto.measurementDtoList) {
                            try {
                                Measurement measurement = new Measurement();
                                measurement.setId(measurementDto.getId()); // ← voorlopig laten staan
                                measurement.setTimestamp(parseTimestamp(measurementDto.getTimestamp()));
                                measurement.setLatitude(measurementDto.getLatitude());
                                measurement.setLongitude(measurementDto.getLongitude());
                                measurement.setTemperature(measurementDto.getTemperature());
                                measurement.setHumidity(measurementDto.getHumidity());
                                measurement.setParticulate(measurementDto.getParticulate());
                                measurement.setIs_public(measurementDto.getIs_public());
                                measurement.setStation(station);

                                measurementsEntityList.add(measurement);
                            } catch (Exception e) {
                                System.err.println("❌ Fout bij converteren van MeasurementDto (stationId: "
                                        + dto.stationid + "): " + e.getMessage());
                                e.printStackTrace(); // zo zie je in je IDE of console waar het fout gaat
                            }
                        }
                    }

                    station.setMeasurementList(measurementsEntityList);
                    return station;
                })
                .toList();



        stationRepository.saveAll(stationEntities);
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

    private static Instant parseTimestamp(String timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);
        return localDateTime.atZone(ZoneId.of("Europe/Amsterdam")).toInstant();
    }

}
