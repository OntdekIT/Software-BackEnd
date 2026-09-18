package Ontdekstation013.ClimateChecker.config;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    // 32 bytes -> 256 bits, geldig voor HS256. Op runtime gegenereerd zodat er
    // geen (schijnbare) sleutel-string in de repo staat.
    private static final String VALID_KEY =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());

    private JwtService jwtService;
    private User user;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService(VALID_KEY);
        user = new User(42L, "Test", "User", "test@example.com", UserRole.USER, "pw");
    }

    @Test
    public void generateAndExtract_roundTrips_theUsername() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        // getUsername() geeft het userId als String terug.
        assertEquals("42", jwtService.extractUsername(token));
    }

    @Test
    public void isTokenValid_true_forFreshTokenAndMatchingUser() {
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.IsTokenValid(token, user));
    }

    @Test
    public void isTokenValid_false_whenUserDoesNotMatch() {
        String token = jwtService.generateToken(user);
        User otherUser = new User(99L, "Other", "User", "other@example.com", UserRole.USER, "pw");

        assertFalse(jwtService.IsTokenValid(token, otherUser));
    }

    @Test
    public void extractUsername_throws_whenTokenSignedWithDifferentKey() {
        String otherKey = Base64.getEncoder().encodeToString("DIFFERENT-key-32bytes-long!!!xyz".getBytes());
        JwtService otherService = new JwtService(otherKey);
        String foreignToken = otherService.generateToken(user);

        // Een token dat met een andere sleutel is ondertekend, mag niet worden vertrouwd.
        assertThrows(Exception.class, () -> jwtService.extractUsername(foreignToken));
    }

    @Test
    public void constructor_throws_whenKeyIsBlank() {
        assertThrows(IllegalStateException.class, () -> new JwtService("  "));
    }

    @Test
    public void constructor_throws_whenKeyIsTooShort() {
        // "c2hvcnQ=" decodeert naar "short" (5 bytes = 40 bit) -> te zwak voor HS256.
        assertThrows(IllegalStateException.class, () -> new JwtService("c2hvcnQ="));
    }
}
