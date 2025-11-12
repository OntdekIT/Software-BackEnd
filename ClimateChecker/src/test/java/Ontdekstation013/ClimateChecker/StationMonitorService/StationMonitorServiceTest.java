package Ontdekstation013.ClimateChecker.StationMonitorService;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationMonitorService;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StationMonitorServiceTest {
    @Mock
    private MeetJeStadService meetJeStadService;

    @Mock
    private StationService stationService;

    @Mock
    private EmailSenderService emailSenderService;

    @InjectMocks
    private StationMonitorService stationMonitorService;

    @Test
    public void testCheckMeetstations_withActiveToInactiveStation_shouldUpdateAndSendEmail() throws Exception {
        // Arrange
        Station station = new Station();
        station.setStationid(1L);
        station.setIsActive(true);
        station.setUser(new User(1L, "FirstName", "LastName", "test@example.com", UserRole.USER, "Password"));
        station.setTempError(false);
        station.setHumError(false);
        station.setStofError(false);
        station.setLocError(false);

        List<Measurement> emptyMeasurements = Collections.emptyList();

        when(stationService.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(emptyMeasurements);

        // Act
        stationMonitorService.checkMeetstations();

        // Assert
        verify(stationService).UpdateMeetstation(station);
        verify(emailSenderService).sendEmailStationDown(any(User.class), eq(station));
        assertFalse(station.getIsActive());
    }

    @Test
    public void testCheckMeetstations_withActiveMeasurements_whenInactive_shouldUpdateAndSendMeasurementUpdateEmail() throws Exception {
        // Arrange
        Station station = new Station();
        station.setStationid(1L);
        station.setIsActive(false);
        station.setUser(new User(1L, "FirstName", "LastName", "test@example.com", UserRole.USER, "Password"));
        station.setTempError(true);
        station.setHumError(true);
        station.setStofError(true);
        station.setLocError(true);

        Measurement m = new Measurement();
        m.setTemperature(22.5f);
        m.setHumidity(55.0f);
        m.setPm25(8.0f);
        m.setPm10(10.0f);
        m.setLatitude(52.0f);
        m.setLongitude(5.0f);

        when(stationService.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(m));

        // Act
        stationMonitorService.checkMeetstations();

        // Assert
        verify(stationService, times(2)).UpdateMeetstation(station); // once for status, once for errors
        verify(emailSenderService).sendEmailStationMeasurements(any(), eq(station), eq(true), eq(true), eq(true), eq(true));
        assertTrue(station.getIsActive());
    }

    @Test
    public void testCheckMeetstations_withActiveMeasurements_whenActive_shouldUpdateAndNotSendEmail() throws Exception {
        // Arrange
        Station station = new Station();
        station.setStationid(1L);
        station.setIsActive(true);
        station.setUser(new User(1L, "FirstName", "LastName", "test@example.com", UserRole.USER, "Password"));
        station.setTempError(false);
        station.setHumError(false);
        station.setStofError(false);
        station.setLocError(false);

        Measurement m = new Measurement();
        m.setTemperature(22.5f);
        m.setHumidity(55.0f);
        m.setPm25(8.0f);
        m.setPm10(10.0f);
        m.setLatitude(52.0f);
        m.setLongitude(5.0f);

        when(stationService.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(m));

        // Act
        stationMonitorService.checkMeetstations();

        // Assert
        verify(stationService).UpdateMeetstation(station);
        verify(emailSenderService, never()).sendEmailStationMeasurements(any(), eq(station), eq(null), eq(null), eq(null), eq(null));
        assertFalse(station.getTempError());
        assertFalse(station.getHumError());
        assertFalse(station.getStofError());
        assertFalse(station.getLocError());
        assertTrue(station.getIsActive());
    }

    @Test
    public void testCheckMeetstations_withActiveMeasurements_whenErrors_shouldUpdateAndSendMeasurementUpdateEmail() throws Exception {
        // Arrange
        Station station = new Station();
        station.setStationid(1L);
        station.setIsActive(true);
        station.setUser(new User(1L, "FirstName", "LastName", "test@example.com", UserRole.USER, "Password"));
        station.setTempError(true);
        station.setHumError(true);
        station.setStofError(true);
        station.setLocError(true);

        Measurement m = new Measurement();
        m.setTemperature(22.5f);
        m.setHumidity(55.0f);
        m.setPm25(8.0f);
        m.setPm10(10.0f);
        m.setLatitude(52.0f);
        m.setLongitude(5.0f);

        when(stationService.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(m));

        // Act
        stationMonitorService.checkMeetstations();

        // Assert
        verify(stationService).UpdateMeetstation(station);
        verify(emailSenderService).sendEmailStationMeasurements(any(), eq(station), eq(true), eq(true), eq(true), eq(true));
        assertTrue(station.getIsActive());
    }

    @Test
    public void testCheckMeetstations_withInactiveMeasurements_whenNoErrors_shouldUpdateAndSendMeasurementUpdateEmail() throws Exception {
        // Arrange
        Station station = new Station();
        station.setStationid(1L);
        station.setIsActive(true);
        station.setUser(new User(1L, "FirstName", "LastName", "test@example.com", UserRole.USER, "Password"));
        station.setTempError(false);
        station.setHumError(false);
        station.setStofError(false);
        station.setLocError(false);

        Measurement m = new Measurement();
        m.setTemperature(null);
        m.setHumidity(55.0f);
        m.setPm25(8.0f);
        m.setPm10(10.0f);
        m.setLatitude(52.0f);
        m.setLongitude(5.0f);

        when(stationService.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(m));

        // Act
        stationMonitorService.checkMeetstations();

        // Assert
        verify(stationService).UpdateMeetstation(station);
        verify(emailSenderService).sendEmailStationMeasurements(any(), eq(station), eq(false), eq(null), eq(null), eq(null));
        assertTrue(station.getIsActive());
    }

    @Test
    public void testCheckMeetstations_withInactiveMeasurements_whenErrors_shouldNotUpdateAndNotSendEmail() throws Exception {
        // Arrange
        Station station = new Station();
        station.setStationid(1L);
        station.setIsActive(true);
        station.setUser(new User(1L, "FirstName", "LastName", "test@example.com", UserRole.USER, "Password"));
        station.setTempError(true);
        station.setHumError(true);
        station.setStofError(true);
        station.setLocError(false);

        Measurement m = new Measurement();
        m.setTemperature(null);
        m.setHumidity(null);
        m.setPm25(null);
        m.setPm10(null);
        m.setLatitude(52.0f);
        m.setLongitude(5.0f);

        when(stationService.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(m));

        // Act
        stationMonitorService.checkMeetstations();

        // Assert
        verify(stationService).UpdateMeetstation(station);
        verify(emailSenderService, never()).sendEmailStationMeasurements(any(), eq(station), eq(null), eq(null), eq(null), eq(null));
        assertTrue(station.getIsActive());
    }

    @Test
    public void testCheckMeetstations_withActiveMeasurementsAndActiveMeetstation_shouldNotUpdateAndNotSendEmail() throws Exception {
        // Arrange
        Station station = new Station();
        station.setStationid(1L);
        station.setIsActive(true);
        station.setUser(new User(1L, "FirstName", "LastName", "test@example.com", UserRole.USER, "Password"));
        station.setTempError(false);
        station.setHumError(false);
        station.setStofError(false);
        station.setLocError(false);

        Measurement m = new Measurement();
        m.setTemperature(22.5f);
        m.setHumidity(55.0f);
        m.setPm25(8.0f);
        m.setPm10(10.0f);
        m.setLatitude(52.0f);
        m.setLongitude(5.0f);

        when(stationService.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(m));

        // Act
        stationMonitorService.checkMeetstations();

        // Assert
        verify(stationService).UpdateMeetstation(station);
        verify(emailSenderService, never()).sendEmailStationMeasurements(any(), eq(station), eq(null), eq(null), eq(null), eq(null));
        assertTrue(station.getIsActive());
    }
}
