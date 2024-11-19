package Ontdekstation013.ClimateChecker.workshop;

import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopRepository;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkshopUnitTests {
    @Mock
    private WorkshopRepository workshopRepository;

    @InjectMocks
    private WorkshopService workshopService;

    @Test
    public void createWorkshopCode_GeneratesRandom_Succeeds() {
        // Arrange
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(1);
        Workshop workshop = new Workshop(123L, expirationDate);
        when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

        // Act
        Workshop createdWorkshop = workshopService.createWorkshop(expirationDate);

        // Assert
        assertNotNull(createdWorkshop);
        assertEquals(123L, createdWorkshop.getCode());
        assertEquals(expirationDate, createdWorkshop.getExpirationDate());
        verify(workshopRepository, times(1)).save(any(Workshop.class));
    }
}
