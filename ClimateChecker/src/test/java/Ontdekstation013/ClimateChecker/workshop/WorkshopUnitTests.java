package Ontdekstation013.ClimateChecker.workshop;

import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopRepository;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkshopUnitTests {
    @Mock
    private WorkshopRepository workshopRepository;

    @InjectMocks
    private WorkshopService workshopService;

    private Workshop workshop;

    @BeforeEach
    public void setUp() {
        workshop = new Workshop(123L, LocalDateTime.now().plusDays(1));
    }

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

    @Test
    public void getAllActiveWorkshops_ReturnsActiveWorkshops() {
        // Arrange
        Workshop activeWorkshop = new Workshop(124L, LocalDateTime.now().plusDays(1));
        when(workshopRepository.findAll()).thenReturn(Arrays.asList(workshop, activeWorkshop));

        // Act
        List<Workshop> activeWorkshops = workshopService.getAllActiveWorkshops();

        // Assert
        assertNotNull(activeWorkshops);
        assertEquals(2, activeWorkshops.size());
        verify(workshopRepository, times(1)).findAll();
    }

    @Test
    public void getAllExpiredWorkshops_ReturnsExpiredWorkshops() {
        // Arrange
        Workshop expiredWorkshop = new Workshop(125L, LocalDateTime.now().minusDays(1));
        when(workshopRepository.findByExpirationDateBefore(any(LocalDateTime.class))).thenReturn(Collections.singletonList(expiredWorkshop));

        // Act
        List<Workshop> expiredWorkshops = workshopService.getAllExpiredWorkshops();

        // Assert
        assertNotNull(expiredWorkshops);
        assertEquals(1, expiredWorkshops.size());
        verify(workshopRepository, times(1)).findByExpirationDateBefore(any(LocalDateTime.class));
    }

    @Test
    public void getByCode_ReturnsWorkshop() {
        // Arrange
        when(workshopRepository.findByCode(123L)).thenReturn(workshop);

        // Act
        Workshop foundWorkshop = workshopService.getByCode(123L);

        // Assert
        assertNotNull(foundWorkshop);
        assertEquals(workshop.getCode(), foundWorkshop.getCode());
        verify(workshopRepository, times(1)).findByCode(123L);
    }

    @Test
    public void deleteWorkshopCode_Succeeds() {
        // Arrange
        when(workshopRepository.findByCode(123L)).thenReturn(workshop);
        doNothing().when(workshopRepository).delete(workshop);

        // Act
        workshopService.deleteWorkshopCode(123L);

        // Assert
        verify(workshopRepository, times(1)).findByCode(123L);
        verify(workshopRepository, times(1)).delete(workshop);
    }

    @Test
    public void verifyWorkshopCode_ReturnsTrueForValidCode() {
        // Arrange
        when(workshopRepository.findByCode(123L)).thenReturn(workshop);

        // Act
        boolean isValid = workshopService.verifyWorkshopCode(123L);

        // Assert
        assertTrue(isValid);
        verify(workshopRepository, times(1)).findByCode(123L);
    }

    @Test
    public void verifyWorkshopCode_ReturnsFalseForInvalidCode() {
        // Arrange
        when(workshopRepository.findByCode(123L)).thenReturn(null);

        // Act
        boolean isValid = workshopService.verifyWorkshopCode(123L);

        // Assert
        assertFalse(isValid);
        verify(workshopRepository, times(1)).findByCode(123L);
    }
}
