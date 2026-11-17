package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;

import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoordsRepository;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodRepository;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Compressie zit in de ingebouwde webserver (Tomcat), dus MockMvc ziet haar niet. Deze test start
 * de applicatie echt op een willekeurige poort en praat er met een echte HTTP-client mee.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.config.import=file:src/main/resources/application.properties",
        "spring.profiles.active=test",
        "rate.limit.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:compressiontestdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
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
class CompressionApiTest {

    private static final long FIRST_ID = 770_000L;
    private static final int NEIGHBOURHOODS = 30;

    @LocalServerPort
    private int port;

    @Autowired
    private Ontdekstation013.ClimateChecker.snapshot.SnapshotService snapshotService;

    @Autowired
    private NeighbourhoodRepository neighbourhoodRepository;

    @Autowired
    private NeighbourhoodCoordsRepository coordsRepository;

    @MockBean
    private EmailSenderService emailSenderService;

    private final List<Neighbourhood> created = new ArrayList<>();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void createSomeNeighbourhoods() {
        // Genoeg wijken met coordinaten dat het antwoord ruim boven de compressiedrempel komt.
        for (int i = 0; i < NEIGHBOURHOODS; i++) {
            Neighbourhood neighbourhood = neighbourhoodRepository.save(new Neighbourhood(FIRST_ID + i, "Wijk " + i));
            created.add(neighbourhood);
            for (int c = 0; c < 8; c++) {
                coordsRepository.save(new NeighbourhoodCoords(
                        (FIRST_ID + i) * 100 + c, 51.5f + c * 0.001f, 5.0f + c * 0.001f, neighbourhood));
            }
        }
        // De snapshot bevat de wijken pas na een herbouw.
        snapshotService.rebuild();
    }

    @AfterEach
    void cleanUp() {
        for (Neighbourhood neighbourhood : created) {
            coordsRepository.deleteAll(coordsRepository.findAll().stream()
                    .filter(c -> c.getNeighbourhood().getId() == neighbourhood.getId()).toList());
        }
        neighbourhoodRepository.deleteAll(created);
    }

    private HttpResponse<byte[]> get(String acceptEncoding) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(
                "http://127.0.0.1:" + port + "/api/neighbourhood/history?timestamp=2026-03-01T12:30:00Z"));
        if (acceptEncoding != null) {
            request.header("Accept-Encoding", acceptEncoding);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    @Test
    void jsonIsGzippedWhenTheClientAsksForIt() throws Exception {
        HttpResponse<byte[]> response = get("gzip");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Encoding")).hasValue("gzip");
        // De inhoud is na uitpakken gewoon de JSON-lijst met wijken.
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(response.body()))) {
            JsonNode body = new ObjectMapper().readTree(gzip.readAllBytes());
            assertThat(body.isArray()).isTrue();
            assertThat(body.size()).isGreaterThanOrEqualTo(NEIGHBOURHOODS);
        }
    }

    @Test
    void jsonIsPlainWhenTheClientDoesNotAcceptGzip() throws Exception {
        HttpResponse<byte[]> response = get(null);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Encoding")).isEmpty();
        assertThat(new ObjectMapper().readTree(response.body()).isArray()).isTrue();
    }

    @Test
    void compressedBodyIsSmallerThanThePlainBody() throws Exception {
        assertThat(get("gzip").body().length).isLessThan(get(null).body().length);
    }

    @Test
    void theSnapshotEndpointIsGzippedAndAnswersNotModifiedForItsEtag() throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/snapshot"))
                .header("Accept-Encoding", "gzip");
        HttpResponse<byte[]> response = client.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Encoding")).hasValue("gzip");
        String etag = response.headers().firstValue("ETag").orElseThrow();
        HttpResponse<byte[]> second = client.send(request.header("If-None-Match", etag).build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(second.statusCode()).isEqualTo(304);
    }
}
