package Ontdekstation013.ClimateChecker.user.authentication;

import Ontdekstation013.ClimateChecker.features.user.authentication.Token;
import Ontdekstation013.ClimateChecker.features.user.authentication.TokenRepository;
import Ontdekstation013.ClimateChecker.features.user.authentication.TokenService;
import Ontdekstation013.ClimateChecker.features.user.authentication.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private Token token(String code, TokenType type, LocalDateTime created) {
        Token t = new Token();
        t.setUserId(1L);
        t.setTokenType(type);
        t.setNumericCode(code);
        t.setCreationTime(created);
        return t;
    }

    @Test
    public void verifyToken_true_whenCodeMatchesAndWithinWindow() {
        Token t = token("123456", TokenType.VERIFY_AUTH, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByUserIdAndTokenType(1L, TokenType.VERIFY_AUTH)).thenReturn(t);

        boolean result = tokenService.verifyToken("123456", 1L, TokenType.VERIFY_AUTH);

        assertTrue(result);
        // Een gebruikte code wordt altijd verwijderd (eenmalig gebruik).
        verify(tokenRepository).delete(t);
    }

    @Test
    public void verifyToken_false_whenCodeDoesNotMatch() {
        Token t = token("123456", TokenType.VERIFY_AUTH, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByUserIdAndTokenType(1L, TokenType.VERIFY_AUTH)).thenReturn(t);

        boolean result = tokenService.verifyToken("000000", 1L, TokenType.VERIFY_AUTH);

        assertFalse(result);
        // Verkeerde code -> token blijft staan (niet verwijderd).
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    public void verifyToken_false_whenTokenExpired() {
        // Ouder dan 5 minuten -> buiten het venster.
        Token t = token("123456", TokenType.VERIFY_AUTH, LocalDateTime.now().minusMinutes(6));
        when(tokenRepository.findByUserIdAndTokenType(1L, TokenType.VERIFY_AUTH)).thenReturn(t);

        boolean result = tokenService.verifyToken("123456", 1L, TokenType.VERIFY_AUTH);

        assertFalse(result);
        // Correcte maar verlopen code wordt wel opgeruimd.
        verify(tokenRepository).delete(t);
    }

    @Test
    public void verifyToken_false_whenNoTokenExists() {
        when(tokenRepository.findByUserIdAndTokenType(1L, TokenType.VERIFY_AUTH)).thenReturn(null);

        assertFalse(tokenService.verifyToken("123456", 1L, TokenType.VERIFY_AUTH));
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    public void createVerifyToken_generatesSixDigitCode_andReplacesExisting() {
        when(tokenRepository.findAllByUserIdAndTokenType(1L, TokenType.PASSWORD_RESET))
                .thenReturn(List.of(token("999999", TokenType.PASSWORD_RESET, LocalDateTime.now())));

        Token created = tokenService.createVerifyToken(1L, TokenType.PASSWORD_RESET);

        assertNotNull(created.getNumericCode());
        assertEquals(6, created.getNumericCode().length());
        assertTrue(created.getNumericCode().matches("\\d{6}"));
        assertEquals(TokenType.PASSWORD_RESET, created.getTokenType());
        // Oude token(s) worden verwijderd en de nieuwe wordt opgeslagen.
        verify(tokenRepository).deleteAll(anyList());
        verify(tokenRepository).save(created);
    }

    @Test
    public void getTokenByUserIdAndTokenType_prunesExpiredTokens() {
        Token expired = token("111111", TokenType.VERIFY_AUTH, LocalDateTime.now().minusMinutes(10));
        when(tokenRepository.findAllByUserIdAndTokenType(1L, TokenType.VERIFY_AUTH))
                .thenReturn(List.of(expired));
        when(tokenRepository.findByUserIdAndTokenType(1L, TokenType.VERIFY_AUTH)).thenReturn(null);

        tokenService.getTokenByUserIdAndTokenType(1L, TokenType.VERIFY_AUTH);

        verify(tokenRepository).delete(expired);
    }
}
