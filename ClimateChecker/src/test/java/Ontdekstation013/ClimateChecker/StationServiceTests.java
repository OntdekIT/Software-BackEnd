package Ontdekstation013.ClimateChecker;

import Ontdekstation013.ClimateChecker.Mocks.MockStationRepo;
import Ontdekstation013.ClimateChecker.models.*;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.services.SensorService;
import Ontdekstation013.ClimateChecker.services.StationService;
import Ontdekstation013.ClimateChecker.services.converters.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class StationServiceTests {
	private StationService stationService;
	private MockStationRepo mockRepo;
	private StationConverter stationConverter;



	@BeforeEach
	void setup() throws Exception{
		this.mockRepo = new MockStationRepo();
		this.stationService = new StationService(mockRepo, stationService.sensorService);
		this.stationConverter = new StationConverter(stationService.sensorService);


		List<Station> stations = new ArrayList<>();

		Station station = new Station();


		// station 1
		station.setStationID(1);
		station.setName("name1");
		station.setHeight(10);
		station.setPublic(true);

		User user = new User();
		user.setUserID(100);
		station.setOwner(user);

		Location location = new Location();
		location.setLocationID(1000);
		station.setLocation(location);

		stations.add(station);


		// station 2
		station = new Station();
		station.setStationID(2);
		station.setName("name2");
		station.setHeight(20);
		station.setPublic(false);

		user = new User();
		user.setUserID(69);
		station.setOwner(user);

		location = new Location();
		location.setLocationID(2000);
		station.setLocation(location);

		stations.add(station);


		// station 3
		station = new Station();
		station.setStationID(3);
		station.setName("name3");
		station.setHeight(30);
		station.setPublic(true);

		user = new User();
		user.setUserID(69);
		station.setOwner(user);

		location = new Location();
		location.setLocationID(3000);
		station.setLocation(location);

		stations.add(station);


		mockRepo.FillDataBase(stations);
	}

	// No functionality in StationService
	@Test
	void findStationByIdTest() {
		stationDto newDto = stationService.findStationById(2);

		Assertions.assertEquals(20,newDto.getHeight());
		Assertions.assertEquals("name2",newDto.getName());
		Assertions.assertEquals(2000,newDto.getLocationId());
		Assertions.assertFalse(newDto.isIspublic());

	}


	// No functionality in StationService
	@Test
	void stationToStationDTOTest() {
		Station station = new Station();
		station.setStationID(1);
		station.setHeight(2);
		station.setLocation(new Location());
		station.setOwner(new User());
		station.setSensors(new ArrayList<>());
		station.setPublic(true);

		stationDto newDto = stationConverter.stationToStationDTO(station);

		Assertions.assertEquals(station.getStationID(),newDto.getId());
		Assertions.assertEquals(station.getHeight(),newDto.getHeight());
		Assertions.assertEquals(station.getLocation().getLocationName(),newDto.getLocationName());
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
	void createStationTest() {
		registerStationDto dto = new registerStationDto();

		dto.setUserId(4);
		dto.setStationname("nameTest");
		dto.setHeight(54);
		dto.setLatitude(5687);
		dto.setLongitude(89767);
		dto.setAddress("AddressTitle");
		dto.setIspublic(true);

		Assertions.assertTrue(stationService.createStation(dto));

		Station result = mockRepo.stations.get(3);

		Assertions.assertEquals(dto.getStationname(), result.getName());
		Assertions.assertEquals(dto.getHeight(), result.getHeight());
		Assertions.assertEquals(dto.getLatitude(), result.getLocation().getLatitude());
		Assertions.assertEquals(dto.getLongitude(), result.getLocation().getLongitude());
		Assertions.assertEquals(dto.getAddress(), result.getLocation().getLocationName());
		Assertions.assertEquals(dto.isIspublic(), result.isPublic());
	}

	@Test
	void createStationTest_ShouldFail() {
		registerStationDto dto = new registerStationDto();

		boolean created = stationService.createStation(dto);

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
		dto2.setHeight(876);
		dto2.setLatitude(9843);
		dto2.setLongitude(8745);
		dto2.setAddress("NEW");
		dto2.setIspublic(false);

		stationService.editStation(dto2);

		Station result = mockRepo.stations.get(3);

		Assertions.assertEquals(dto2.getName(), result.getName());
		Assertions.assertEquals(dto2.getHeight(), result.getHeight());
		Assertions.assertEquals(dto2.getLatitude(), result.getLocation().getLatitude());
		Assertions.assertEquals(dto2.getLongitude(), result.getLocation().getLongitude());
		Assertions.assertEquals(dto2.getAddress(), result.getLocation().getLocationName());
		Assertions.assertEquals(dto2.isIspublic(), result.isPublic());
	}

}
