package Ontdekstation013.ClimateChecker.station;

import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StationUnitTests {

    private Station station;
    private StationDto dto;

    @BeforeEach
    public void setUp() {
        // Arrange: set up a Station and StationDto with fixed test values
        station = new Station(
                "TestStation",
                "DB_TAG_001",
                true,
                12345L,
                1L,
                42L,
                true,
                false,
                false,
                true,
                false
        );

        station.setStationid(99L); // ID is normally auto-generated, but for testing we assign it

        dto = new StationDto(
                99L,
                "TestStation",
                "DB_TAG_001",
                true,
                12345L,
                1L,
                42L,
                true,
                false,
                false,
                true,
                false
        );
    }

    @Test
    public void testStationConstructor() {
        // Arrange
        String expectedName = "TestStation";
        String expectedDatabaseTag = "DB_TAG_001";
        Boolean expectedIsPublic = true;
        Long expectedRegistrationCode = 12345L;
        Long expectedLocationId = 1L;
        Long expectedUserId = 42L;
        Boolean expectedIsActive = true;
        Boolean expectedTempError = false;
        Boolean expectedHumError = true;
        Boolean expectedStofError = true;
        Boolean expectedLocError = false;

        // Act
        Station station = new Station(
                expectedName,
                expectedDatabaseTag,
                expectedIsPublic,
                expectedRegistrationCode,
                expectedLocationId,
                expectedUserId,
                expectedIsActive,
                expectedTempError,
                expectedHumError,
                expectedStofError,
                expectedLocError
        );

        // Assert
        assertEquals(expectedName, station.getName());
        assertEquals(expectedDatabaseTag, station.getDatabase_tag());
        assertEquals(expectedIsPublic, station.getIs_public());
        assertEquals(expectedRegistrationCode, station.getRegistrationCode());
        assertEquals(expectedLocationId, station.getLocation_locationid());
        assertEquals(expectedUserId, station.getUserid());
        assertEquals(expectedIsActive, station.getIsActive());
        assertEquals(expectedTempError, station.getTempError());
        assertEquals(expectedHumError, station.getHumError());
        assertEquals(expectedStofError, station.getStofError());
        assertEquals(expectedLocError, station.getLocError());
    }

    @Test
    public void testStationToStationDtoConstructor() {
        // Act
        StationDto result = station.toDto();

        // Assert
        assertEquals(dto.stationid, station.getStationid());
        assertEquals(dto.name, station.getName());
        assertEquals(dto.database_tag, station.getDatabase_tag());
        assertEquals(dto.is_public, station.getIs_public());
        assertEquals(dto.registrationCode, station.getRegistrationCode());
        assertEquals(dto.location_locationid, station.getLocation_locationid());
        assertEquals(dto.userid, station.getUserid());
        assertEquals(dto.isActive, station.getIsActive());
        assertEquals(dto.tempError, station.getTempError());
        assertEquals(dto.humError, station.getHumError());
        assertEquals(dto.stofError, station.getStofError());
        assertEquals(dto.locError, station.getLocError());
    }

    @Test
    public void testStationDtoToStationConstructor() {
        // Act
        Station newStation = new Station(dto);

        // Assert
        assertEquals(dto.stationid, station.getStationid());
        assertEquals(dto.name, station.getName());
        assertEquals(dto.database_tag, station.getDatabase_tag());
        assertEquals(dto.is_public, station.getIs_public());
        assertEquals(dto.registrationCode, station.getRegistrationCode());
        assertEquals(dto.location_locationid, station.getLocation_locationid());
        assertEquals(dto.userid, station.getUserid());
        assertEquals(dto.isActive, station.getIsActive());
        assertEquals(dto.tempError, station.getTempError());
        assertEquals(dto.humError, station.getHumError());
        assertEquals(dto.stofError, station.getStofError());
        assertEquals(dto.locError, station.getLocError());
    }
}
