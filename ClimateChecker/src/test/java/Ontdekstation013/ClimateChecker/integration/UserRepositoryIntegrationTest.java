package Ontdekstation013.ClimateChecker.integration;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integratietest tegen een echte MariaDB (Testcontainers): controleert dat de
 * Flyway-migraties draaien, de seed-admin wordt aangemaakt en de custom
 * repository-queries + JPA-mapping werken tegen een echte database.
 */
public class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void seededAdmin_existsAfterStartup() {
        // De DataSeeder maakt bij het opstarten een root-admin aan.
        User admin = userRepository.findByEmail("admin@example.com");

        assertNotNull(admin, "De seed-admin zou moeten bestaan na startup");
        assertEquals("admin@example.com", admin.getEmail());
        assertTrue(userRepository.existsUserByEmail("admin@example.com"));
    }

    @Test
    public void saveAndFindByEmail_roundTripsThroughRealDatabase() {
        User user = new User("Piet", "Puk", "piet.puk@example.com", "hash", null);
        user.setRole(UserRole.USER);
        userRepository.save(user);

        User found = userRepository.findByEmail("piet.puk@example.com");

        assertNotNull(found);
        assertEquals("Piet", found.getFirstName());
        assertNotNull(found.getUserId(), "ID wordt door de database gegenereerd");
    }

    @Test
    public void findUsersByOptionalFilters_paginatesAndFiltersOnRealDatabase() {
        for (int i = 0; i < 3; i++) {
            User u = new User("Filter", "Test" + i, "filter" + i + "@example.com", "hash", null);
            u.setRole(UserRole.USER);
            userRepository.save(u);
        }

        // Filter op voornaam "Filter", eerste pagina van 2.
        Page<User> page = userRepository.findUsersByOptionalFilters(
                "Filter", null, null, null, PageRequest.of(0, 2));

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.getContent().stream().allMatch(u -> u.getFirstName().equals("Filter")));
    }

    @Test
    public void findUsersByOptionalFilters_withoutFilters_returnsAllUsers() {
        // Minimaal de seed-admin.
        Page<User> page = userRepository.findUsersByOptionalFilters(
                null, null, null, null, PageRequest.of(0, 50));

        assertTrue(page.getTotalElements() >= 1);
    }
}
