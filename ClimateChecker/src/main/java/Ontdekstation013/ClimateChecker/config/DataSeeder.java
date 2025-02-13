package Ontdekstation013.ClimateChecker.config;

import Ontdekstation013.ClimateChecker.features.user.PasswordEncodingService;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for seeding the database with initial data.
 * This class is only active when the 'test' profile is not active.
 */
@RequiredArgsConstructor
@Configuration
@Profile("!test")
public class DataSeeder {
    private final PasswordEncodingService passwordEncodingService;

    @Value("${application.root-user.email}")
    private String rootUserEmail;

    @Value("${application.root-user.first-name}")
    private String rootUserFirstName;

    @Value("${application.root-user.last-name}")
    private String rootUserLastName;

    @Value("${application.root-user.password}")
    private String rootUserPassword;

    /**
     * Seeds the database with a root user if the database is empty.
     * Note: the application won't start if the root user info isn't present in the secrets.properties file.
     */
    @Bean
    CommandLineRunner seedDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User user = new User();
                user.setEmail(rootUserEmail);
                user.setFirstName(rootUserFirstName);
                user.setLastName(rootUserLastName);
                user.setRole(UserRole.SUPER_ADMIN);
                user.setPassword(passwordEncodingService.encodePassword(rootUserPassword));
                userRepository.save(user);
            }
        };
    }
}