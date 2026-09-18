package Ontdekstation013.ClimateChecker.workshop;

import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopMapper;
import Ontdekstation013.ClimateChecker.features.workshop.endpoint.dto.WorkshopResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class WorkshopMapperTest {

    @Test
    public void toResponse_mapsCode_andFormatsDatesAsUtc() {
        Workshop w = new Workshop();
        w.setCode(123456L);
        w.setExpirationDate(LocalDateTime.of(2026, 6, 1, 12, 0));
        w.setCreationTime(LocalDateTime.of(2026, 5, 1, 8, 30));

        WorkshopResponse response = WorkshopMapper.toResponse(w);

        assertEquals(123456L, response.code());
        // Datums worden als UTC-offset (Z) geformatteerd.
        assertEquals("2026-06-01T12:00Z", response.expirationDate());
        assertEquals("2026-05-01T08:30Z", response.creationDate());
    }

    @Test
    public void toResponse_allowsNullCreationTime() {
        Workshop w = new Workshop();
        w.setCode(654321L);
        w.setExpirationDate(LocalDateTime.of(2026, 6, 1, 12, 0));
        w.setCreationTime(null);

        WorkshopResponse response = WorkshopMapper.toResponse(w);

        assertEquals(654321L, response.code());
        assertNull(response.creationDate());
    }
}
