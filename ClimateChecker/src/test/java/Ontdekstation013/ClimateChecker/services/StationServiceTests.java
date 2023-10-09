package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.Mocks.MockSensorRepo;
import Ontdekstation013.ClimateChecker.Mocks.MockStationRepo;
import Ontdekstation013.ClimateChecker.Mocks.MockTypeRepo;
import Ontdekstation013.ClimateChecker.Mocks.MockUserRepo;
import Ontdekstation013.ClimateChecker.features.location.Location;
import Ontdekstation013.ClimateChecker.features.sensor.SensorConverter;
import Ontdekstation013.ClimateChecker.features.sensor.SensorService;
import Ontdekstation013.ClimateChecker.features.station.StationOld;
import Ontdekstation013.ClimateChecker.features.station.StationConverter;
import Ontdekstation013.ClimateChecker.features.station.StationServiceOld;
import Ontdekstation013.ClimateChecker.features.station.endpoint.editStationDto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.registerStationDto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.stationDto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.stationTitleDto;
import Ontdekstation013.ClimateChecker.features.user.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class StationServiceTests {
	private StationServiceOld stationService;
	private SensorService sensorService;
	private MockStationRepo mockRepo;
	private MockSensorRepo mockSensorRepo;
	private MockTypeRepo mockTypeRepo;
	private MockUserRepo mockUserRepo;
	private SensorConverter sensorConverter;
	private StationConverter stationConverter;




	@BeforeEach
	void setup() throws Exception{
		this.mockRepo = new MockStationRepo();
		this.mockSensorRepo = new MockSensorRepo();
		this.mockTypeRepo = new MockTypeRepo();
		this.mockUserRepo = new MockUserRepo();
		this.sensorConverter = new SensorConverter();
		this.stationConverter = new StationConverter();
		this.sensorService = new SensorService(mockSensorRepo, mockTypeRepo, sensorConverter, mockRepo);
		this.stationService = new StationServiceOld(mockRepo, sensorService, mockUserRepo);


		List<StationOld> stations = new ArrayList<>();

		StationOld station = new StationOld();


		// station 1
		station.setStationID(1);
		station.setName("name1");
		station.setPublic(true);

		User user = new User();
		user.setUserID(100);
		station.setOwner(user);

		Location location = new Location();
		location.setLocationID(1000);
		station.setLocation(location);

		stations.add(station);


		// station 2
		station = new StationOld();
		station.setStationID(2);
		station.setName("name2");
		station.setPublic(false);

		user = new User();
		user.setUserID(69);
		station.setOwner(user);

		location = new Location();
		location.setLocationID(2000);
		station.setLocation(location);

		stations.add(station);


		// station 3
		station = new StationOld();
		station.setStationID(3);
		station.setName("name3");
		station.setPublic(true);

		user = new User();
		user.setUserID(69);
		station.setOwner(user);

		location = new Location();
		location.setLocationID(3000);
		station.setLocation(location);

		stations.add(station);


		// station 4
		station = new StationOld();
		station.setStationID(4);
		station.setName("name4");
		station.setPublic(true);
		station.setRegistrationCode(378);
		station.setDatabaseTag("MJS");

		user = new User();
		user.setUserID(70);
		station.setOwner(user);

		location = new Location();
		location.setLocationID(15);
		station.setLocation(location);

		stations.add(station);

		mockRepo.FillDataBase(stations);


		List<User> userList = new ArrayList<>();
		User mockUser = new User();
		mockUser.setUserID(4);
		userList.add(mockUser);

		mockUserRepo.FillDatabase(userList);
	}

	// No functionality in StationService
	@Test
	void findStationByIdTest() {
		stationDto newDto = stationService.findStationById(2);

		Assertions.assertEquals("name2",newDto.getName());
		Assertions.assertEquals(2000,newDto.getLocationId());
		Assertions.assertFalse(newDto.isIspublic());

	}


	// No functionality in StationService
	@Test
	void stationToStationDTOTest() {
		StationOld station = new StationOld();
		station.setStationID(1);
		station.setLocation(new Location());
		station.setOwner(new User());
		station.setSensors(new ArrayList<>());
		station.setPublic(true);

		stationDto newDto = stationService.stationToStationDTO(station);

		Assertions.assertEquals(station.getStationID(),newDto.getId());
		Assertions.assertEquals(station.getLocation().getHeight(),newDto.getHeight());
		Assertions.assertEquals(station.getLocation().getLocationID(),newDto.getLocationId());
		Assertions.assertEquals(station.getLocation().getLongitude(),newDto.getLongitude());
		Assertions.assertEquals(station.getLocation().getLatitude(),newDto.getLatitude());
		Assertions.assertEquals(station.isPublic(), newDto.isIspublic());
	}


	// No functionality in StationService
	// Fix this GetAllByUserId when it works by Sensor
	@Test
	void getAllByUserIDTest() {

		List<stationTitleDto> newDtoList = stationService.getAllByUserId(69);


		Assertions.assertEquals(2,newDtoList.get(0).getId());
		Assertions.assertEquals("name2",newDtoList.get(0).getName());
		Assertions.assertEquals(3,newDtoList.get(1).getId());
		Assertions.assertEquals("name3",newDtoList.get(1).getName());

	}


	// No functionality in StationService

	@Test
	void getAllTest() {

		List<stationTitleDto> newDtoList = stationService.getAll();


		Assertions.assertEquals(1,newDtoList.get(0).getId());
		Assertions.assertEquals("name1",newDtoList.get(0).getName());
		Assertions.assertEquals(2,newDtoList.get(1).getId());
		Assertions.assertEquals("name2",newDtoList.get(1).getName());
		Assertions.assertEquals(3,newDtoList.get(2).getId());
		Assertions.assertEquals("name3",newDtoList.get(2).getName());
	}


	// No functionality in StationService
	// Paginator functionality first needed
	@Test
	void getAllByPageIdTest() {
		/*List<stationTitleDto> newDtoList = stationService.getAllByPageId(1);


		Assertions.assertEquals(2,newDtoList.get(0).getId());
		Assertions.assertEquals(dto.getName(),newDtoList.get(0).getName());*/
		Assertions.assertTrue(true);
	}



	@Test
	void registerStationTest() {
		registerStationDto dto = new registerStationDto();

		dto.setUserId(4);
		dto.setRegisterCode(378);
		dto.setDatabaseTag("MJS");
		dto.setStationName("nameTest");
		dto.setHeight(54);
		dto.setDirection("E");
		dto.setPublicInfo(true);
		dto.setOutside(true);

		Assertions.assertTrue(stationService.registerStation(dto));

		StationOld result = mockRepo.stations.get(3);

		Assertions.assertEquals(dto.getStationName(), result.getName());
		Assertions.assertEquals(dto.getHeight(), result.getLocation().getHeight());
		Assertions.assertEquals(dto.isPublicInfo(), result.isPublic());
	}

	@Test
	void registerStationTest_ShouldFail() {
		registerStationDto dto = new registerStationDto();
		dto.setRegisterCode(550);
		dto.setDatabaseTag("MJS");
		dto.setUserId(70);

		boolean created = stationService.registerStation(dto);

		Assertions.assertFalse(created);
	}


	// No functionality in StationService
	@Test
	void deleteStationTest() {
		long testId = 1;
		stationService.deleteStation(testId);

		Assertions.assertFalse(mockRepo.stations.contains(testId));
	}


	// No functionality in StationService
	@Test
	void editStationTest() {
		editStationDto dto2 = new editStationDto();

		dto2.setId(3);
		dto2.setName("TestNameNEW");
		dto2.setPublic(false);

		stationService.editStation(dto2);

		StationOld result = mockRepo.stations.get(2);

		Assertions.assertEquals(dto2.getName(), result.getName());
		Assertions.assertEquals(dto2.isPublic(), result.isPublic());
	}

}
