package Ontdekstation013.ClimateChecker.station;

import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationFilter;
import Ontdekstation013.ClimateChecker.features.station.StationMapper;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.dto.GetAllStationsRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StationMapperTest {

    private Station sampleStation() {
        Station s = new Station();
        s.setStationid(7L);
        s.setName("Meetstation 7");
        s.setDatabase_tag("MJS");
        s.setIs_public(true);
        s.setRegistrationCode(4242L);
        s.setLocation_locationid(3L);
        s.setUserid(5L);
        s.setIsActive(true);
        s.setTempError(false);
        s.setHumError(true);
        s.setStofError(false);
        s.setLocError(true);
        return s;
    }

    @Test
    public void toStationDTO_mapsAllFields() {
        StationDto dto = StationMapper.toStationDTO(sampleStation());

        assertEquals(7L, dto.stationid);
        assertEquals("Meetstation 7", dto.name);
        assertEquals("MJS", dto.database_tag);
        assertTrue(dto.is_public);
        assertEquals(4242L, dto.registrationCode);
        assertEquals(3L, dto.location_locationid);
        assertEquals(5L, dto.userid);
        assertTrue(dto.isActive);
        assertFalse(dto.tempError);
        assertTrue(dto.humError);
        assertFalse(dto.stofError);
        assertTrue(dto.locError);
    }

    @Test
    public void toStationFilter_mapsRequestFields() {
        GetAllStationsRequest request = new GetAllStationsRequest(
                0, 20, "Naam", "MJS", true, 123L, 9L, "piet", false);

        StationFilter filter = StationMapper.toStationFilter(request);

        assertEquals("Naam", filter.getName());
        assertEquals("MJS", filter.getDatabaseTag());
        assertTrue(filter.getIsPublic());
        assertEquals(123L, filter.getRegistrationCode());
        assertEquals("piet", filter.getUsername());
        assertFalse(filter.getIsActive());
    }
}
