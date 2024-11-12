package Ontdekstation013.ClimateChecker.features.user;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class PasswordUtils {
    public static String HashPassword(String password)
    {
        int saltLength = 16;
        int hashLength = 32;
        int parallelism = 8;
        int memory = 65536;
        int iterations = 4;

        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);

        return encoder.encode(password);
    }

    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();
        return encoder.matches(rawPassword, hashedPassword);
    }
}
