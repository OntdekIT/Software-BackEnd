package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.exception.ExistingUniqueIdentifierException;
import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
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
        if (stationRepository.existsById(station.getStationid())) {
            throw new InvalidArgumentException("Station-id is al in gebruik!");
        }
        if (stationRepository.getByRegistrationCode(station.getRegistrationCode()) != null) {
            throw new InvalidArgumentException("Registratiecode is al in gebruik!");
        }
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

    /**
     * Claimt een nog niet geclaimd station voor {@code station.getUserid()}. Een station
     * dat al een eigenaar heeft wordt geweigerd (overnemen gaat via transfer). De
     * registratiecode blijft ongewijzigd; alleen naam en zichtbaarheid worden overgenomen.
     */
    public void ClaimMeetstation(Station station) {
        Station existingStation = stationRepository.getMeetstationByStationid(station.getStationid());
        if (existingStation == null) {
            throw new NotFoundException("Station not found");
        }
        if (existingStation.getUserid() != null) {
            throw new ExistingUniqueIdentifierException("Station is al geclaimd");
        }
        existingStation.setUserid(station.getUserid());
        if (station.getName() != null && !station.getName().isBlank()) {
            existingStation.setName(station.getName());
        }
        if (station.getIs_public() != null) {
            existingStation.setIs_public(station.getIs_public());
        }
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

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    public List<Station> getAllStations(StationFilter filter) {
        List<Long> userIds = resolveUserIds(filter);
        if (userIds != null && userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return stationRepository.findStationsByOptionalFilters(filter.getName(), filter.getDatabaseTag(), filter.getIsPublic(), filter.getRegistrationCode(), userIds, filter.getIsActive());
    }

    /** Paged variant of {@link #getAllStations}, ordered by station id. Uses {@code filter.getPage()} and {@code filter.getPageSize()}. */
    public Page<Station> getStationsPage(StationFilter filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int pageSize = filter.getPageSize() != null ? filter.getPageSize() : DEFAULT_PAGE_SIZE;
        if (page < 0) {
            throw new InvalidArgumentException("page must be 0 or greater");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new InvalidArgumentException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("stationid"));
        List<Long> userIds = resolveUserIds(filter);
        if (userIds != null && userIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return stationRepository.findStationsByOptionalFilters(filter.getName(), filter.getDatabaseTag(), filter.getIsPublic(), filter.getRegistrationCode(), userIds, filter.getIsActive(), pageable);
    }

    /**
     * Combines the {@code userid} filter and the {@code username} filter. Returns null when neither
     * is set (no user restriction) and an empty list when they match no user (no stations).
     */
    private List<Long> resolveUserIds(StationFilter filter) {
        List<Long> userIds = filter.getUserIds();
        if (filter.getUsername() != null && !filter.getUsername().isEmpty()) {
            List<Long> usernameIds = userRepository.findByFullName(filter.getUsername()).stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());
            if (userIds == null) {
                userIds = usernameIds;
            } else {
                userIds = userIds.stream().filter(usernameIds::contains).collect(Collectors.toList());
            }
        }
        return userIds;
    }

    @Async
    public CompletableFuture<List<StationDto>> getStationsWithMeasurements(Instant timestamp) {
        return CompletableFuture.completedFuture(assembleStations(measurementService.getMeasurementsAtTime(timestamp)));
    }

    /** Alle stations uit de database met de gegeven metingen eraan gekoppeld (zonder cache, voor snapshots). */
    public List<StationDto> getStationsWithMeasurementsUncached(Instant timestamp) {
        return assembleStations(measurementService.computeMeasurementsAtTime(timestamp));
    }

    private List<StationDto> assembleStations(List<MeasurementDto> measurements) {
        List<Station> stations = stationRepository.findAll();

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
        //Groepeer measurements op StationID
        Map<Long, List<MeasurementDto>> measurementsById = measurements.stream().collect(Collectors.groupingBy(m -> Long.valueOf(m.getId())));
        //Maak lijst aan unieke StationIDs van alle measurements
        List<Long> uniqueIds = measurements.stream().map(MeasurementDto::getId).map(Long::valueOf).distinct().toList();
        //Haal alle stations uit database op en maak er DTOs van
        List<StationDto> stationsDB = new ArrayList<>();
        for (Station station : stationRepository.findAllById(uniqueIds))
        {
            stationsDB.add(StationMapper.toStationDTO(station));
        }
        //Haal alle Ids op van stations die al bestaan
        Set<Long> existingStationIds = stationsDB.stream().map(stationDto -> stationDto.stationid).collect(Collectors.toSet());
        //Maak stationDTOs voor elke unieke measurement StationID die nog geen stationDTO heeft en voeg toe aan lijst alle stationDTOs
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
                        false,
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
                boolean alreadyExists = station.measurementDtoList.stream().anyMatch(m -> m.getTimestamp().equals(measurementDto.getTimestamp()));
                //Als de station die niet heeft voeg je die measurementdto toe aan die stationdto
                if (!alreadyExists) {
                    station.measurementDtoList.add(measurementDto);
                }
            }
        }


        List<Station> stationEntities = stationsDB.stream()
                .map(dto -> {
                    Station station = stationRepository.findById(dto.stationid)
                            .orElseGet(() -> {
                                Station newStation = new Station();
                                newStation.setStationid(dto.stationid);
                                return newStation;
                            });

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
                                measurement.setStation(station); 
                                measurement.setTimestamp(parseTimestamp(measurementDto.getTimestamp()));
                                measurement.setLatitude(measurementDto.getLatitude());
                                measurement.setLongitude(measurementDto.getLongitude());
                                measurement.setLocationInherited(measurementDto.isLocationInherited());
                                measurement.setTemperature(measurementDto.getTemperature());
                                measurement.setHumidity(measurementDto.getHumidity());
                                measurement.setPm25(measurementDto.getPm25());
                                measurement.setPm10(measurementDto.getPm10());
                                measurement.setIs_public(measurementDto.getIs_public());

                                measurementsEntityList.add(measurement);
                            } catch (Exception e) {
                                log.warn("Fout bij converteren van MeasurementDto (stationId: {})", dto.stationid, e);
                            }
                        }
                    }

                    station.setMeasurementList(measurementsEntityList);
                    return station;
                })
                .toList();

        //Sla de stations met measurements op in de lokale DB
        try {
            stationRepository.saveAll(stationEntities);
            log.info("Synchronisatie afgerond: {} station(s) met metingen opgeslagen", stationEntities.size());
        } catch (Exception e) {
            log.error("Fout bij het opslaan van stations tijdens de synchronisatie", e);
        }
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

    /**
     * Draagt een meetstation over aan een nieuwe eigenaar (op basis van diens
     * e-mailadres). De meethistorie blijft behouden omdat metingen aan het
     * station hangen, niet aan de gebruiker; alleen de eigenaar-verwijzing
     * verandert.
     */
    @Transactional
    public void transferOwnership(long stationId, String newOwnerEmail) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station not found"));

        User newOwner = userRepository.findByEmail(newOwnerEmail);
        if (newOwner == null) {
            throw new NotFoundException("Gebruiker met dit e-mailadres bestaat niet");
        }

        station.setUserid(newOwner.getUserId());
        stationRepository.save(station);
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
