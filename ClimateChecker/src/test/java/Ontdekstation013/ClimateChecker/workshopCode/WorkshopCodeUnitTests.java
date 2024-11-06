package Ontdekstation013.ClimateChecker.workshopCode;

import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCode;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeRepository;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeService;
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
public class WorkshopCodeUnitTests {
    @Mock
    private WorkshopCodeRepository workshopCodeRepository;

    @InjectMocks
    private WorkshopCodeService workshopCodeService;

    @Test
    public void createWorkshopCode_GeneratesRandom_Succeeds() {
        WorkshopCode workshopCode = new WorkshopCode(123L, LocalDateTime.now().plusDays(1));
        when(workshopCodeRepository.save(any(WorkshopCode.class))).thenReturn(workshopCode);

        WorkshopCode createdWorkshopCode = workshopCodeService.createWorkshopCode(LocalDateTime.now());

        assertNotNull(createdWorkshopCode);
        assertEquals(123L, createdWorkshopCode.getCode());
        verify(workshopCodeRepository, times(1)).save(any(WorkshopCode.class));
    }

    @Test
    public void getAllWorkshopCodes_Succeeds() {
        List<WorkshopCode> workshopCodes = Arrays.asList(
                new WorkshopCode(123L, LocalDateTime.now().plusDays(1)),
                new WorkshopCode(456L, LocalDateTime.now().plusDays(2))
        );
        when(workshopCodeRepository.findAll()).thenReturn(workshopCodes);

        List<WorkshopCode> result = workshopCodeService.getAllWorkshopCodes();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(workshopCodeRepository, times(1)).findAll();
    }

    @Test
    public void deleteWorkshopCodeOnScheduledTaskWhenExpired() {
        WorkshopCode expiredCode = new WorkshopCode(123L, LocalDateTime.now().minusDays(1));
        when(workshopCodeRepository.findById(123L)).thenReturn(Optional.of(expiredCode));

        workshopCodeService.deleteExpiredWorkshopCodes();

        verify(workshopCodeRepository, times(1)).deleteById(123L);
    }
}
