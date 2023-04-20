package Ontdekstation013.ClimateChecker;

import Ontdekstation013.ClimateChecker.models.*;
import Ontdekstation013.ClimateChecker.repositories.*;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ClimateCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClimateCheckerApplication.class, args);
	}



	@Bean
	CommandLineRunner run(LocationRepository locationRepository, TypeRepository typeRepository, RegionCordsRepository regionCordsRepository, RegionRepository regionRepository, SensorRepository sensorRepository, UserRepository userRepository, StationRepository stationRepository) {
		return args -> {

			//Seed users
			List<User> users = new ArrayList<>();
			users.add(new User(1L, "Pieter", "Peter", "Pieter@mail.com", "PieterPeter", false));
			users.add(new User(2L, "Jan", "Joep", "Jan@mail.com", "JanJoep", true));
			users.add(new User(3L, "Joeri", "waterman", "Joeri@mail.com", "JoeriWaterman", false));
			users.add(new User(4L, "Benny", "Bener", "Benny@mail.com", "BennyBener", false));
			users.add(new User(5L, "Janny", "Jansen", "Janny@mail.com", "JannyJansen", false));

			for (User user: users) {
				userRepository.save(user);
			}



			List<Region> regions = new ArrayList<>();
			regions.add(new Region(1L, "Tilburg Noord"));
			regions.add(new Region(2L, "Tilburg Oud-Noord"));
			regions.add(new Region(3L, "Tilburg West"));
			regions.add(new Region(4L, "Tilburg Reeshof"));
			regions.add(new Region(5L, "Tilburg Centrum"));
			regions.add(new Region(6L, "Tilburg Zuid"));

			//Seed Regions
			for (Region region: regions) {
				regionRepository.save(region);
			}

			//Seed Region Coords

			regionCordsRepository.save(new RegionCords(1L, regions.get(0), 51.56255066080151, 5.110574072153875));
			regionCordsRepository.save(new RegionCords(2L, regions.get(0), 51.578729761919675, 5.089185307344954));
			regionCordsRepository.save(new RegionCords(3L, regions.get(0), 51.58073572699174, 5.051152783432803));
			regionCordsRepository.save(new RegionCords(4L, regions.get(0), 51.597919306791404, 5.07069963024435));
			regionCordsRepository.save(new RegionCords(5L, regions.get(0), 51.57888096194896, 5.123793169211084));

			regionCordsRepository.save(new RegionCords(6L, regions.get(1), 51.56306306758613, 5.107730565503828));
			regionCordsRepository.save(new RegionCords(7L, regions.get(1), 51.560220503931724, 5.088965879026096));
			regionCordsRepository.save(new RegionCords(8L, regions.get(1), 51.56316143871074, 5.065365901887907));
			regionCordsRepository.save(new RegionCords(9L, regions.get(1), 51.57828930027731, 5.066310843694255));
			regionCordsRepository.save(new RegionCords(10L, regions.get(1), 51.57799353698609, 5.090601412591779));

			regionCordsRepository.save(new RegionCords(11L, regions.get(2), 51.57850240619557, 5.065901358218249));
			regionCordsRepository.save(new RegionCords(12L, regions.get(2), 51.58289461361321, 5.027460773334913));
			regionCordsRepository.save(new RegionCords(13L, regions.get(2), 51.569364501176715, 5.0236293100389195));
			regionCordsRepository.save(new RegionCords(14L, regions.get(2), 51.560472853296794, 5.025591312563916));
			regionCordsRepository.save(new RegionCords(15L, regions.get(2), 51.557497903138355, 5.049635448082193));
			regionCordsRepository.save(new RegionCords(16L, regions.get(2), 51.556539031553335, 5.063734032998137));

			regionCordsRepository.save(new RegionCords(17L, regions.get(3), 51.58284664423249, 5.0273944712514975));
			regionCordsRepository.save(new RegionCords(18L, regions.get(3), 51.60042319421235, 4.982040344334981));
			regionCordsRepository.save(new RegionCords(19L, regions.get(3), 51.59192444270981, 4.974106947760719));
			regionCordsRepository.save(new RegionCords(20L, regions.get(3), 51.5700097218146, 4.967688018248374));
			regionCordsRepository.save(new RegionCords(21L, regions.get(3), 51.56237932491169, 5.016013009511919));

			regionCordsRepository.save(new RegionCords(22L, regions.get(4), 51.56270129182065, 5.065284371322884));
			regionCordsRepository.save(new RegionCords(23L, regions.get(4), 51.55617127808693, 5.065133469119954));
			regionCordsRepository.save(new RegionCords(24L, regions.get(4), 51.55412292501397, 5.0855311053269885));
			regionCordsRepository.save(new RegionCords(25L, regions.get(4), 51.54992904431306, 5.0876111787297855));
			regionCordsRepository.save(new RegionCords(26L, regions.get(4), 51.55531861612618, 5.105617226979377));
			regionCordsRepository.save(new RegionCords(27L, regions.get(4), 51.560652616815936, 5.10329437013893));
			regionCordsRepository.save(new RegionCords(28L, regions.get(4), 51.55945789444568, 5.091095283523403));

			regionCordsRepository.save(new RegionCords(29L, regions.get(5), 51.55784684502422, 5.041274079951624));
			regionCordsRepository.save(new RegionCords(30L, regions.get(5), 51.54281546677295, 5.064465419727073));
			regionCordsRepository.save(new RegionCords(31L, regions.get(5), 51.53820174172339, 5.063035560222271));
			regionCordsRepository.save(new RegionCords(32L, regions.get(5), 51.53609745524849, 5.098102508316022));
			regionCordsRepository.save(new RegionCords(33L, regions.get(5), 51.543196233257596, 5.112926401276529));
			regionCordsRepository.save(new RegionCords(34L, regions.get(5), 51.55103707996244, 5.110116072477741));
			regionCordsRepository.save(new RegionCords(35L, regions.get(5), 51.55310267677778, 5.114121361718272));
			regionCordsRepository.save(new RegionCords(36L, regions.get(5), 51.556811513500826, 5.111992692817807));
			regionCordsRepository.save(new RegionCords(37L, regions.get(5), 51.549448466861485, 5.087879389017815));
			regionCordsRepository.save(new RegionCords(38L, regions.get(5), 51.55400163051958, 5.085270726191843));
			regionCordsRepository.save(new RegionCords(39L, regions.get(5), 51.55544653351087, 5.073630409560555));





			//Seed SensorType
			List<SensorType> sensorTypes = new ArrayList<>();

			sensorTypes.add(new SensorType(1L, "Temperatuur"));
			sensorTypes.add(new SensorType(2L, "Stikstof"));
			sensorTypes.add(new SensorType(3L, "Koolstofdioxide"));
			sensorTypes.add(new SensorType(4L, "Fijnstof"));
			sensorTypes.add(new SensorType(5L, "Luchtvochtigheid"));
			sensorTypes.add(new SensorType(6L, "Windsnelheid"));

			for (SensorType sensorType: sensorTypes) {
				typeRepository.save(sensorType);
			}


			//Seed Location
			List<Location> locations = new ArrayList<>();

			locations.add(new Location(1L, (float)51.581124, (float)4.994231, (float) 1.0, "N"));
			locations.add(new Location(2L, (float)51.575043, (float)5.002305, (float) 6.0, "E"));
			locations.add(new Location(3L, (float)51.539151, (float)5.079001, (float) 38.2, "N"));
			locations.add(new Location(4L, (float)51.56664652915646, (float)5.0888906124543665, (float) 2.0, "E"));
			locations.add(new Location(5L, (float)51.58398517610992, (float)5.086270119955303, (float) 4.0,"S"));
			locations.add(new Location(6L, (float)51.55800402393493, (float)5.092794917662567, (float) 10.0,"W"));
			locations.add(new Location(7L, (float)51.58102914210408, (float)5.092454772670171, (float) 10.0,"E"));

			for (Location location : locations) {
				locationRepository.save(location);
			}

			//Seed Station
			List<Station> stations = new ArrayList<>();

			stations.add(new Station(1L, 4,"Voortuin", locations.get(0), users.get(0), true));
			stations.add(new Station(2L, 60,"Industrieterrein", locations.get(1), users.get(0), false));
			stations.add(new Station(3L, 33,"Fontys dak", locations.get(2), users.get(1), true));
			stations.add(new Station(4L, 2,"Besterdplein", locations.get(3), users.get(1), true));
			stations.add(new Station(5L, 11,"Wagnerplein", locations.get(4), users.get(2), true));
			stations.add(new Station(6L, 98,"013 Poppodium", locations.get(5), users.get(3), true));
			stations.add(new Station(7L, 51,"Heikantlaan", locations.get(6), users.get(4), true));

			for (Station station : stations) {
				stationRepository.save(station);
			}

			//Seed Sensor
			sensorRepository.save(new Sensor(1L, 10, sensorTypes.get(0), stations.get(0)));
			sensorRepository.save(new Sensor(2L, 12, sensorTypes.get(0), stations.get(1)));
			sensorRepository.save(new Sensor(3L, 9, sensorTypes.get(0), stations.get(2)));
			sensorRepository.save(new Sensor(4L, 1, sensorTypes.get(1), stations.get(0)));
			sensorRepository.save(new Sensor(5L, 5, sensorTypes.get(1), stations.get(1)));
			sensorRepository.save(new Sensor(6L, 3, sensorTypes.get(1), stations.get(2)));
			sensorRepository.save(new Sensor(7L, 4, sensorTypes.get(2), stations.get(0)));

			sensorRepository.save(new Sensor(8L, 6, sensorTypes.get(2), stations.get(1)));
			sensorRepository.save(new Sensor(9L, 2, sensorTypes.get(1), stations.get(1)));
			sensorRepository.save(new Sensor(10L, 4, sensorTypes.get(3), stations.get(0)));
			sensorRepository.save(new Sensor(11L, 1, sensorTypes.get(3), stations.get(1)));
			sensorRepository.save(new Sensor(12L, 1, sensorTypes.get(3), stations.get(2)));

			sensorRepository.save(new Sensor(13L, 10, sensorTypes.get(4), stations.get(0)));
			sensorRepository.save(new Sensor(14L, 1, sensorTypes.get(4), stations.get(1)));
			sensorRepository.save(new Sensor(15L, 4, sensorTypes.get(5), stations.get(2)));
			sensorRepository.save(new Sensor(16L, 6, sensorTypes.get(5), stations.get(0)));
			sensorRepository.save(new Sensor(17L, 5, sensorTypes.get(5), stations.get(1)));

			sensorRepository.save(new Sensor(19L, 12, sensorTypes.get(0), stations.get(3)));
			sensorRepository.save(new Sensor(20L, 13, sensorTypes.get(0), stations.get(4)));
			sensorRepository.save(new Sensor(21L, 9, sensorTypes.get(0), stations.get(5)));
			sensorRepository.save(new Sensor(22L, 14, sensorTypes.get(0), stations.get(6)));

		};
	}
}

