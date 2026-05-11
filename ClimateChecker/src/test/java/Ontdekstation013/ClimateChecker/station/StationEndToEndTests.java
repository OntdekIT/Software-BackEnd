package Ontdekstation013.ClimateChecker.station;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StationEndToEndTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String url(String path) {
        return "http://localhost:" + port + "/api/Meetstation" + path;
    }

    @Test
    void testGetAllStationsSpeed() {
        long start = System.currentTimeMillis();

        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/stations"), String.class);

        long duration = System.currentTimeMillis() - start;

        System.out.println("GET /stations took: " + duration + "ms");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(duration).isLessThan(2000); // fail if slower than 2 seconds
    }

    @Test
    void testGetStationsWithMeasurementsSpeed() {
        String timestamp = Instant.now().toString();
        long start = System.currentTimeMillis();

        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/stationsMetMeasurements?timestamp=" + timestamp), String.class);

        long duration = System.currentTimeMillis() - start;

        System.out.println("GET /stationsMetMeasurements took: " + duration + "ms");
        assertThat(duration).isLessThan(3000);
    }

    @Test
    void testGetStationByIdSpeed() {
        long start = System.currentTimeMillis();

        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/1"), String.class);

        long duration = System.currentTimeMillis() - start;

        System.out.println("GET /station/1 took: " + duration + "ms");
        assertThat(duration).isLessThan(1000);
    }
}