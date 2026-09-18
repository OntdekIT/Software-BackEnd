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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkshopServiceTest {

    @Mock
    private WorkshopRepository workshopRepository;

    @InjectMocks
    private WorkshopService workshopService;

    private Workshop workshop(long code, LocalDateTime expiration) {
        Workshop w = new Workshop();
        w.setCode(code);
        w.setExpirationDate(expiration);
        w.setCreationTime(LocalDateTime.now());
        return w;
    }

    @Test
    public void verifyWorkshopCode_true_whenCodeExistsAndNotExpired() {
        when(workshopRepository.findByCode(123456L))
                .thenReturn(workshop(123456L, LocalDateTime.now().plusDays(1)));

        assertTrue(workshopService.verifyWorkshopCode(123456L));
    }

    @Test
    public void verifyWorkshopCode_false_whenCodeExpired() {
        when(workshopRepository.findByCode(123456L))
                .thenReturn(workshop(123456L, LocalDateTime.now().minusDays(1)));

        assertFalse(workshopService.verifyWorkshopCode(123456L));
    }

    @Test
    public void verifyWorkshopCode_false_whenCodeDoesNotExist() {
        when(workshopRepository.findByCode(999999L)).thenReturn(null);

        assertFalse(workshopService.verifyWorkshopCode(999999L));
    }

    @Test
    public void getAllActiveWorkshops_returnsOnlyNonExpired() {
        Workshop active = workshop(1L, LocalDateTime.now().plusDays(1));
        Workshop expired = workshop(2L, LocalDateTime.now().minusDays(1));
        when(workshopRepository.findAll()).thenReturn(List.of(active, expired));

        List<Workshop> result = workshopService.getAllActiveWorkshops();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCode());
    }

    @Test
    public void createWorkshop_generatesCode_andSaves() {
        when(workshopRepository.existsById(any())).thenReturn(false);
        when(workshopRepository.save(any(Workshop.class))).thenAnswer(i -> i.getArgument(0));

        LocalDateTime expiration = LocalDateTime.now().plusDays(7);
        Workshop created = workshopService.createWorkshop(expiration);

        assertNotNull(created.getCode());
        assertTrue(created.getCode() >= 100000 && created.getCode() <= 999999);
        assertEquals(expiration, created.getExpirationDate());
        verify(workshopRepository).save(created);
    }

    @Test
    public void deleteWorkshopCode_deletesTheFoundWorkshop() {
        Workshop w = workshop(123456L, LocalDateTime.now().plusDays(1));
        when(workshopRepository.findByCode(123456L)).thenReturn(w);

        workshopService.deleteWorkshopCode(123456L);

        verify(workshopRepository).delete(w);
    }
}
