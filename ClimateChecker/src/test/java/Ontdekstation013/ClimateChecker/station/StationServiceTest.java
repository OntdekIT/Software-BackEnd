package Ontdekstation013.ClimateChecker.station;

import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MeasurementService measurementService;

    @InjectMocks
    private StationService stationService;

    private Station stationWithId(long id, Long ownerId) {
        Station station = new Station();
        station.setStationid(id);
        station.setName("TestStation");
        station.setDatabase_tag("MJS");
        station.setIs_public(true);
        station.setRegistrationCode(123L);
        station.setUserid(ownerId);
        station.setIsActive(true);
        station.setTempError(false);
        station.setHumError(false);
        station.setStofError(false);
        station.setLocError(false);
        return station;
    }

    @Test
    public void readById_returnsDto_whenStationExists() {
        when(stationRepository.getMeetstationByStationid(1L)).thenReturn(stationWithId(1L, 5L));

        StationDto dto = stationService.ReadById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.stationid);
    }

    @Test
    public void readById_returnsNull_whenStationMissing() {
        when(stationRepository.getMeetstationByStationid(99L)).thenReturn(null);

        assertNull(stationService.ReadById(99L));
    }

    @Test
    public void isAvailable_true_whenStationHasNoOwner() {
        when(stationRepository.findById(1L)).thenReturn(Optional.of(stationWithId(1L, null)));

        assertTrue(stationService.IsAvailable(1L));
    }

    @Test
    public void isAvailable_false_whenStationAlreadyClaimed() {
        when(stationRepository.findById(1L)).thenReturn(Optional.of(stationWithId(1L, 7L)));

        assertFalse(stationService.IsAvailable(1L));
    }

    @Test
    public void isAvailable_false_whenStationDoesNotExist() {
        when(stationRepository.findById(42L)).thenReturn(Optional.empty());

        assertFalse(stationService.IsAvailable(42L));
    }

    @Test
    public void getStationById_returnsStation_whenExists() {
        Station station = stationWithId(3L, 8L);
        when(stationRepository.findById(3L)).thenReturn(Optional.of(station));

        assertSame(station, stationService.GetStationById(3L));
    }

    @Test
    public void getStationById_throws_whenMissing() {
        when(stationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stationService.GetStationById(404L));
    }

    @Test
    public void getByRegistrationCode_delegatesToRepository() {
        Station station = stationWithId(2L, 6L);
        when(stationRepository.getByRegistrationCode(123L)).thenReturn(station);

        assertSame(station, stationService.getByRegistrationCode(123L));
    }
}
