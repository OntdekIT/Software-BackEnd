package Ontdekstation013.ClimateChecker.user.authentication;

import Ontdekstation013.ClimateChecker.features.user.authentication.trustedip.TrustedIp;
import Ontdekstation013.ClimateChecker.features.user.authentication.trustedip.TrustedIpRepository;
import Ontdekstation013.ClimateChecker.features.user.authentication.trustedip.TrustedIpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrustedIpServiceTest {

    @Mock
    private TrustedIpRepository trustedIpRepository;

    private TrustedIpService trustedIpService;

    @BeforeEach
    public void setUp() {
        trustedIpService = new TrustedIpService(trustedIpRepository, "test-pepper");
    }

    @Test
    public void isTrusted_true_whenHashExistsForUser() {
        when(trustedIpRepository.existsByUserIdAndIpHash(eq(1L), anyString())).thenReturn(true);

        assertTrue(trustedIpService.isTrusted(1L, "1.2.3.4"));
    }

    @Test
    public void isTrusted_false_whenUnknownIp() {
        when(trustedIpRepository.existsByUserIdAndIpHash(eq(1L), anyString())).thenReturn(false);

        assertFalse(trustedIpService.isTrusted(1L, "9.9.9.9"));
    }

    @Test
    public void isTrusted_false_whenIpIsNullOrBlank() {
        assertFalse(trustedIpService.isTrusted(1L, null));
        assertFalse(trustedIpService.isTrusted(1L, "  "));
        verifyNoInteractions(trustedIpRepository);
    }

    @Test
    public void remember_savesHashedIp_notThePlainIp() {
        when(trustedIpRepository.existsByUserIdAndIpHash(eq(1L), anyString())).thenReturn(false);

        trustedIpService.remember(1L, "1.2.3.4");

        ArgumentCaptor<TrustedIp> saved = ArgumentCaptor.forClass(TrustedIp.class);
        verify(trustedIpRepository).save(saved.capture());
        assertEquals(1L, saved.getValue().getUserId());
        // Het opgeslagen adres is gehasht, dus niet gelijk aan het rauwe IP.
        assertNotEquals("1.2.3.4", saved.getValue().getIpHash());
        assertNotNull(saved.getValue().getCreatedAt());
    }

    @Test
    public void remember_isIdempotent_whenAlreadyKnown() {
        when(trustedIpRepository.existsByUserIdAndIpHash(eq(1L), anyString())).thenReturn(true);

        trustedIpService.remember(1L, "1.2.3.4");

        verify(trustedIpRepository, never()).save(any());
    }

    @Test
    public void hash_isStableForSameIp_andDifferentPerIp() {
        // Zelfde IP -> zelfde uitkomst (anders zou isTrusted nooit matchen).
        when(trustedIpRepository.existsByUserIdAndIpHash(eq(1L), anyString())).thenReturn(true);
        ArgumentCaptor<String> h1 = ArgumentCaptor.forClass(String.class);
        trustedIpService.isTrusted(1L, "1.2.3.4");
        verify(trustedIpRepository).existsByUserIdAndIpHash(eq(1L), h1.capture());

        String firstHash = h1.getValue();
        assertNotEquals("1.2.3.4", firstHash);
        assertFalse(firstHash.isBlank());
    }
}
