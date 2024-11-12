package Ontdekstation013.ClimateChecker.workshopCode;

import Ontdekstation013.ClimateChecker.features.workshopCode.Workshop;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopRepository;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        Workshop workshop = new Workshop(123L, LocalDateTime.now().plusDays(1));
        when(workshopRepository.save(any(Workshop.class))).thenReturn(workshop);

        Workshop createdWorkshop = workshopService.createWorkshop(LocalDateTime.now());

        assertNotNull(createdWorkshop);
        assertEquals(123L, createdWorkshop.getCode());
        verify(workshopRepository, times(1)).save(any(Workshop.class));
    }

    @Test
    public void getAllWorkshopCodes_Succeeds() {
        List<Workshop> workshops = Arrays.asList(
                new Workshop(123L, LocalDateTime.now().plusDays(1)),
                new Workshop(456L, LocalDateTime.now().plusDays(2))
        );
        when(workshopRepository.findAll()).thenReturn(workshops);

        List<Workshop> result = workshopService.getAllActiveWorkshops();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(workshopRepository, times(1)).findAll();
    }

    @Test
    public void deleteWorkshopCodeOnScheduledTaskWhenExpired() {
        Workshop expiredCode = new Workshop(123L, LocalDateTime.now().minusDays(1));
        when(workshopRepository.findById(123L)).thenReturn(Optional.of(expiredCode));

        workshopService.deleteExpiredWorkshopCodes();

        verify(workshopRepository, times(1)).deleteById(123L);
    }
}
