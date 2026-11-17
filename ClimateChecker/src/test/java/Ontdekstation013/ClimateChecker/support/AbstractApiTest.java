package Ontdekstation013.ClimateChecker.support;

import Ontdekstation013.ClimateChecker.config.JwtService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basis voor API-tests door de echte HTTP-laag (MockMvc) met de volledige
 * Spring Security-keten, JWT-authenticatie en de H2-testdatabase. Elke test
 * draait in een transactie die na afloop wordt teruggedraaid.
 */
@SpringBootTest(properties = {
        // src/test/resources/application.properties schaduwt het main-bestand op de classpath;
        // importeer de echte main-properties en overschrijf daarna de omgevingsafhankelijke waarden.
        "spring.config.import=file:src/main/resources/application.properties",
        "spring.profiles.active=test",
        "rate.limit.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:apitestdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "application.root-user.first-name=Admin",
        "application.root-user.last-name=User",
        "application.root-user.email=admin@example.com",
        "application.root-user.password=password"
})
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractApiTest {

    private static final AtomicLong SEQUENCE = new AtomicLong(900_000);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected StationRepository stationRepository;

    // Geen echte mailserver in tests.
    @MockBean
    protected EmailSenderService emailSenderService;

    protected User createUser(UserRole role) {
        long n = SEQUENCE.incrementAndGet();
        User user = new User("Test", "Gebruiker", "user" + n + "@example.com", "geen-echte-hash", null);
        user.setRole(role);
        return userRepository.save(user);
    }

    protected String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    protected Station createStation(Long ownerId) {
        long n = SEQUENCE.incrementAndGet();
        Station station = new Station("Station " + n, "MJS", true, n, null, ownerId, true, false, false, false, false);
        station.setStationid(n);
        return stationRepository.save(station);
    }
}
