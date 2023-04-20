package Ontdekstation013.ClimateChecker.Services;

import Ontdekstation013.ClimateChecker.Mocks.MockLocationRepo;
import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.Station;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class LocationServiceTests {
	private LocationService locationService;
	private MockLocationRepo mockRepo;


	@BeforeEach
	void setup() throws Exception{
		this.mockRepo = new MockLocationRepo();
		this.locationService = new LocationService(mockRepo);

		List<Location> locations = new ArrayList<>();

		Location location = new Location();

		// location 2
		location.setLocationID(1);
		location.setLocationName("name1");
		location.setLatitude(10);
		location.setLongitude(100);

		Station station = new Station();
		station.setStationID(1000);
		location.setStation(station);

		locations.add(location);

		// location 2
		location = new Location();

		location.setLocationID(2);
		location.setLocationName("name2");
		location.setLatitude(20);
		location.setLongitude(200);

		station = new Station();
		station.setStationID(2000);
		location.setStation(station);

		locations.add(location);

		mockRepo.FillDatabase(locations);
	}

	// No functionality in LocationService
	// Make this when it works in Sensor
	@Test
	void findLocationByIdTest() {
		Location location = locationService.findLocationById(1);


		Assertions.assertEquals(location.getLatitude(), 10);
		Assertions.assertEquals(location.getLongitude(), 100);
		Assertions.assertEquals(location.getStation().getStationID(), 1000);


		location = locationService.findLocationById(2);


		Assertions.assertEquals(location.getLatitude(), 20);
		Assertions.assertEquals(location.getLongitude(), 200);
		Assertions.assertEquals(location.getStation().getStationID(), 2000);
	}


	// No functionality in LocationService
	@Test
	void getAllTest() {
		List<Location> locations = locationService.getAll();

		Assertions.assertEquals(1, locations.get(0).getLocationID());
		Assertions.assertEquals("name1", locations.get(0).getLocationName());
		Assertions.assertEquals(10, locations.get(0).getLatitude());
		Assertions.assertEquals(100, locations.get(0).getLongitude());
		Assertions.assertEquals(1000, locations.get(0).getStation().getStationID());


		Assertions.assertEquals(2, locations.get(1).getLocationID());
		Assertions.assertEquals("name2", locations.get(1).getLocationName());
		Assertions.assertEquals(20, locations.get(1).getLatitude());
		Assertions.assertEquals(200, locations.get(1).getLongitude());
		Assertions.assertEquals(2000, locations.get(1).getStation().getStationID());



	}
}
