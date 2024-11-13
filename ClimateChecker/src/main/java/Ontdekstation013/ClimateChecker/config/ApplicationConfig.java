package Ontdekstation013.ClimateChecker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {int saltLength = 16;
        int hashLength = 32;
        int parallelism = 8;
        int memory = 65536;
        int iterations = 4;

        return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
    }
}
