package Ontdekstation013.ClimateChecker.user;

import Ontdekstation013.ClimateChecker.features.user.PasswordEncodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncodingServiceTest {

    private PasswordEncodingService service;

    @BeforeEach
    public void setUp() {
        // Zelfde Argon2-parameters als de applicatieconfiguratie.
        service = new PasswordEncodingService(new Argon2PasswordEncoder(16, 32, 8, 65536, 4));
    }

    @Test
    public void encodePassword_producesArgon2Hash_notThePlaintext() {
        String hash = service.encodePassword("MyPassword123!");

        assertNotNull(hash);
        assertNotEquals("MyPassword123!", hash);
        assertTrue(hash.startsWith("$argon2"));
    }

    @Test
    public void encodePassword_isSalted_soSamePasswordYieldsDifferentHashes() {
        String h1 = service.encodePassword("samePassword");
        String h2 = service.encodePassword("samePassword");

        assertNotEquals(h1, h2);
    }

    @Test
    public void verifyPassword_true_forMatchingPassword() {
        String hash = service.encodePassword("correct horse battery staple");

        assertTrue(service.verifyPassword("correct horse battery staple", hash));
    }

    @Test
    public void verifyPassword_false_forWrongPassword() {
        String hash = service.encodePassword("correct horse battery staple");

        assertFalse(service.verifyPassword("wrong password", hash));
    }
}
