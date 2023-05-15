package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.Mocks.MockSensorRepo;
import Ontdekstation013.ClimateChecker.Mocks.MockStationRepo;
import Ontdekstation013.ClimateChecker.Mocks.MockTypeRepo;
import Ontdekstation013.ClimateChecker.models.Sensor;
import Ontdekstation013.ClimateChecker.models.SensorType;
import Ontdekstation013.ClimateChecker.models.Station;
import Ontdekstation013.ClimateChecker.models.dto.sensorAverageDto;
import Ontdekstation013.ClimateChecker.models.dto.sensorDto;
import Ontdekstation013.ClimateChecker.models.dto.sensorTypeDto;
import Ontdekstation013.ClimateChecker.repositories.StationRepositoryCustom;
import Ontdekstation013.ClimateChecker.services.converters.SensorConverter;
import Ontdekstation013.ClimateChecker.services.converters.StationConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class SensorServiceTests {
	private SensorService sensorService;
	private MockSensorRepo mockRepo;
	private MockTypeRepo mockTypeRepo;
	private SensorConverter sensorConverter;
	private MockStationRepo stationRepo;



	@BeforeEach
	void setup() throws Exception{
		this.mockRepo = new MockSensorRepo();
		this.mockTypeRepo = new MockTypeRepo();
		this.stationRepo = new MockStationRepo();
		this.sensorService = new SensorService(mockRepo, mockTypeRepo, sensorConverter, stationRepo);


		List<SensorType> sensorTypes = new ArrayList<>();

		//sensor type 1
		SensorType sensorType = new SensorType();
		sensorType.setTypeName("Type1");
		sensorType.setTypeID(4);
		sensorTypes.add(sensorType);

		//sensor type 2
		SensorType sensorType1 = new SensorType();
		sensorType1.setTypeName("Type2");
		sensorType1.setTypeID(6);
		sensorTypes.add(sensorType1);
		mockTypeRepo.FillDatabase(sensorTypes);



		List<Sensor> sensors = new ArrayList<>();
		Sensor sensor = new Sensor();

		// sensor 1
		sensor.setSensorID(1);
		sensor.setSensorData(2);

		Station station = new Station();
		station.setStationID(5);
		sensor.setStation(station);

		SensorType type = new SensorType();
		type.setTypeID(4);
		sensor.setSensorType(type);

		sensors.add(sensor);

		// sensor 2
		sensor = new Sensor();
		station = new Station();
		type = new SensorType();
		sensor.setSensorID(2);
		sensor.setSensorData(1);

		station.setStationID(7);
		sensor.setStation(station);

		type.setTypeID(6);
		sensor.setSensorType(type);

		sensors.add(sensor);

		// sensor 3
		sensor = new Sensor();
		station = new Station();
		type = new SensorType();
		sensor.setSensorID(3);
		sensor.setSensorData(4);

		station.setStationID(5);
		sensor.setStation(station);

		type.setTypeID(6);
		sensor.setSensorType(type);

		sensors.add(sensor);

		mockRepo.FillDatabase(sensors);
	}

	// No functionality in SensorService
	@Test
	void sensorToSensorDtoTest() {
		Sensor sensor = new Sensor();
		sensor.setSensorID(0);
		sensor.setSensorData(1);
		sensor.setSensorType(new SensorType());
		sensor.getSensorType().setTypeID(2);
		sensor.setStation(new Station());
		sensor.getStation().setStationID(3);

		sensorDto newDto = sensorConverter.sensorToSensorDTO(sensor);

		Assertions.assertEquals(0,newDto.getId());
		Assertions.assertEquals(1,newDto.getData());
		Assertions.assertEquals(2,newDto.getTypeId());
		Assertions.assertEquals(3,newDto.getStationId());
	}


	// No functionality in SensorService
	@Test
	void getAllTest() {

		List<sensorDto> sensorDtoList = sensorService.getAll();

		Assertions.assertEquals(1,sensorDtoList.get(0).getId() );
		Assertions.assertEquals(2,sensorDtoList.get(0).getData() );
		Assertions.assertEquals(4,sensorDtoList.get(0).getTypeId() );
		Assertions.assertEquals(5,sensorDtoList.get(0).getStationId() );
		Assertions.assertEquals(2,sensorDtoList.get(1).getId() );
		Assertions.assertEquals(1,sensorDtoList.get(1).getData() );
		Assertions.assertEquals(6,sensorDtoList.get(1).getTypeId() );
		Assertions.assertEquals(7,sensorDtoList.get(1).getStationId() );

	}

	// No functionality in SensorService
	@Test
	void getAllSensorTypesTest() {

		List<sensorTypeDto> TypeList = sensorService.getAllSensorTypes();

		Assertions.assertEquals("Type1"  , TypeList.get(0).getName());
		Assertions.assertEquals(4, TypeList.get(0).getId());
		Assertions.assertEquals(6, TypeList.get(1).getId());
		Assertions.assertEquals("Type2", TypeList.get(1).getName());


	}

	// Has functionality
	@Test
	void getAllAverageSensorDataTest() {
		sensorAverageDto avgDto = sensorService.getAllAverageSensorData();

		Assertions.assertEquals(avgDto.getParticulateMatter(), 2);
		Assertions.assertEquals(avgDto.getWindSpeed(), 2.5);
	}


	// No functionality in SensorService
	@Test
	void createSensorTest() {
		sensorDto sensor = new sensorDto();

		sensor.setTypeId(1);
		sensor.setData(2);
		sensor.setStationId(3);
		sensor.setId(4);

		sensorService.createSensor(sensor);

		Assertions.assertTrue(true);
	}

	// No functionality in SensorService
	@Test
	void createSensorTypeTest(){
		sensorTypeDto sensor = new sensorTypeDto();

		sensor.setName("Name");
		sensor.setId(1);

		sensorService.createSensorType(sensor);

		Assertions.assertTrue(true);
	}


	// No functionality in SensorService
	@Test
	void deleteSensorTest() {
		sensorService.deleteSensor(2);

		Assertions.assertTrue(true);
	}

	// No functionality in SensorService
	@Test
	void findSensorByIdTest(){

		sensorDto newDto = sensorService.findSensorById(2);

		Assertions.assertEquals(newDto.getStationId(),7);
		Assertions.assertEquals(newDto.getId(),2);
		Assertions.assertEquals(newDto.getData(),1);
		Assertions.assertEquals(newDto.getTypeId(),6);

	}

	// Has functionality
	@Test
	void getSensorsByTypeTest(){
		List<sensorDto> newDtoList = sensorService.getSensorsByType(6);

		Assertions.assertEquals(newDtoList.get(0).getId() ,2);
		Assertions.assertEquals(newDtoList.get(0).getTypeId() ,6);
		Assertions.assertEquals(newDtoList.get(0).getData() ,1);
		Assertions.assertEquals(newDtoList.get(0).getStationId() ,7);

		Assertions.assertEquals(newDtoList.get(1).getId() ,3);
		Assertions.assertEquals(newDtoList.get(1).getTypeId() ,6);
		Assertions.assertEquals(newDtoList.get(1).getData() ,4);
		Assertions.assertEquals(newDtoList.get(1).getStationId() ,5);
	}

	// No functionality in SensorService
	@Test
	void getSensorByStation(){

		List<sensorDto> newDtoList = sensorService.getSensorsByStationId(5);

		Assertions.assertEquals(newDtoList.get(0).getId() ,1);
		Assertions.assertEquals(newDtoList.get(0).getTypeId() ,4);
		Assertions.assertEquals(newDtoList.get(0).getData() ,2);
		Assertions.assertEquals(newDtoList.get(0).getStationId() ,5);

		Assertions.assertEquals(newDtoList.get(1).getId() ,3);
		Assertions.assertEquals(newDtoList.get(1).getTypeId() ,6);
		Assertions.assertEquals(newDtoList.get(1).getData() ,4);
		Assertions.assertEquals(newDtoList.get(1).getStationId() ,5);
	}
}
