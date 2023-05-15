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



			/*List<Region> regions = new ArrayList<>();
			regions.add(new Region(1L, "Tilburg Noord"));
			regions.add(new Region(2L, "Tilburg Oud-Noord"));
			regions.add(new Region(3L, "Tilburg West"));
			regions.add(new Region(4L, "Tilburg Reeshof"));
			regions.add(new Region(5L, "Tilburg Centrum"));
			regions.add(new Region(6L, "Tilburg Zuid"));
			regions.add(new Region(7L, "Armhoef"));

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


			regionCordsRepository.save(new RegionCords(40L, regions.get(6), 51.5609016, 5.1037319));
			regionCordsRepository.save(new RegionCords(41L, regions.get(6), 51.5614525, 5.1079274));
			regionCordsRepository.save(new RegionCords(42L, regions.get(6), 51.5568922, 5.1106971));
			regionCordsRepository.save(new RegionCords(43L, regions.get(6), 51.555403, 5.106216));*/





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

			locations.add(new Location(1L, "Reeshof",  (float)51.581124, (float)4.994231));
			locations.add(new Location(2L, "Reeshof",  (float)51.575043, (float)5.002305));
			locations.add(new Location(3L, "Stappegoor",  (float)51.539151, (float)5.079001));
			locations.add(new Location(4L, "Besterd",  (float)51.56664652915646, (float)5.0888906124543665));
			locations.add(new Location(5L, "Wagnerplein",  (float)51.58398517610992, (float)5.086270119955303));
			locations.add(new Location(6L, "013 Poppodium",  (float)51.55800402393493, (float)5.092794917662567));
			locations.add(new Location(7L, "Heikantlaan",  (float)51.58102914210408, (float)5.092454772670171));
			locations.add(new Location(8L, "Kinder vakantie werk",  (float)51.5694046, (float)5.0496172));

			for (Location location : locations) {
				locationRepository.save(location);
			}

			//Seed Station
			List<Station> stations = new ArrayList<>();

			stations.add(new Station(1L, "Voortuin", (float) 1.0, locations.get(0), users.get(0), true));
			stations.add(new Station(2L, "Industrieterrein", (float) 6.0, locations.get(1), users.get(0), false));
			stations.add(new Station(3L, "Fontys dak", (float) 38.2, locations.get(2), users.get(1), true));
			stations.add(new Station(4L, "Besterdplein", (float) 2.0, locations.get(3), users.get(1), true));
			stations.add(new Station(5L, "Wagnerplein", (float) 4.0, locations.get(4), users.get(2), true));
			stations.add(new Station(6L, "013 Poppodium", (float) 10.0, locations.get(5), users.get(3), true));
			stations.add(new Station(7L, "Heikantlaan", (float) 10.0, locations.get(6), users.get(4), true));
			stations.add(new Station(8L, "Kinder vakantie werk", (float) 10.0, locations.get(7), users.get(4), true));

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

			sensorRepository.save(new Sensor(23L, 1, sensorTypes.get(0), stations.get(7)));
			sensorRepository.save(new Sensor(24L, 2, sensorTypes.get(1), stations.get(7)));
			sensorRepository.save(new Sensor(25L, 3, sensorTypes.get(2), stations.get(7)));


			List<Region> regions = new ArrayList<>();
			regions.add(new Region(1L, "Stockhasselt"));
			regions.add(new Region(2L, "Besterd"));
			regions.add(new Region(3L, "Broekhoven"));
			regions.add(new Region(4L, "Het Goirke"));
			regions.add(new Region(5L, "De Reit"));
			regions.add(new Region(6L, "Tivoli"));
			regions.add(new Region(7L, "Trouwlaan"));
			regions.add(new Region(8L, "Moerenburg"));
			regions.add(new Region(9L, "Het Zand"));
			regions.add(new Region(10L, "Industrieterrien-oost"));
			regions.add(new Region(11L, "Groenewoud"));
			regions.add(new Region(12L, "Tuindorp De Kievit"));
			regions.add(new Region(13L, "Armhoef"));
			regions.add(new Region(14L, "De Lijnse Hoek"));
			regions.add(new Region(15L, "Korvel"));
			regions.add(new Region(16L, "Zorgvlied"));
			regions.add(new Region(17L, "Jeruzalem"));
			regions.add(new Region(18L, "De Blaak"));
			regions.add(new Region(19L, "Hoefstraat"));
			regions.add(new Region(20L, "Koolhoven"));
			regions.add(new Region(21L, "Witbrant"));
			regions.add(new Region(22L, "Groeseind"));
			regions.add(new Region(23L, "Stokhasselt-zuid"));

			// de onderste wijk is momenteel niet goed en moet nagekeken worden om de juiste naam erbij te zetten
			regions.add(new Region(24L, "Rooi harten"));

			regions.add(new Region(25L, "De Oude Warande"));
			regions.add(new Region(26L, "Wandelbos-zuid"));
			regions.add(new Region(27L, "Koningshoeven"));
			regions.add(new Region(28L, "Hasselt"));
			regions.add(new Region(29L, "Mariaziekenhuis-Vredeburcht"));
			regions.add(new Region(30L, "Heikant"));

			regions.add(new Region(31L, "Dalem"));
			regions.add(new Region(32L, "Het Laar"));

			//alle naamgeving hieronder moet nagekeken worden en op de juite plek gezet worden
			regions.add(new Region(33L, "Vlashof"));

			regions.add(new Region(34L, "Wandelbos-noord"));
			regions.add(new Region(35L, "Stokhasselt-noord"));
			regions.add(new Region(36L, "Huibeven"));
			regions.add(new Region(37L, "Heerevelden"));
			regions.add(new Region(38L, "Industrieterrein Loven"));
			regions.add(new Region(39L, "Campenhoef"));
			regions.add(new Region(40L, "Loven"));
			regions.add(new Region(41L, "Dongewijk"));
			regions.add(new Region(42L, "Gesworen Hoek"));
			regions.add(new Region(43L, "Industriestrook Goirke-Kanaaldijk"));
			regions.add(new Region(44L, "Heyhoef"));
			regions.add(new Region(45L, "De Quirijnstok"));
			regions.add(new Region(46L, "Industriestrook Lovense-Kanaaldijk"));



			//Seed Regions
			for (Region region: regions) {
				regionRepository.save(region);
			}

			//Seed Region Coords

			regionCordsRepository.save(new RegionCords(1L, regions.get(0), 51.578822, 5.066447));
			regionCordsRepository.save(new RegionCords(2L, regions.get(0), 51.578095, 5.073574));
			regionCordsRepository.save(new RegionCords(3L, regions.get(0), 51.593915, 5.081886));
			regionCordsRepository.save(new RegionCords(4L, regions.get(0), 51.596827, 5.071576));
//			regionCordsRepository.save(new RegionCords(5L, regions.get(0), 51.586323, 5.079426));
			regionCordsRepository.save(new RegionCords(6L, regions.get(1), 51.560042,5.091805));
			regionCordsRepository.save(new RegionCords(7L, regions.get(1), 51.561754,5.093154));
			regionCordsRepository.save(new RegionCords(8L, regions.get(1), 51.561874,5.091833));
			regionCordsRepository.save(new RegionCords(9L, regions.get(1), 51.567510,5.088281));
			regionCordsRepository.save(new RegionCords(10L, regions.get(1), 51.567176,5.081810));
			regionCordsRepository.save(new RegionCords(11L, regions.get(1), 51.561027,5.080020));
			regionCordsRepository.save(new RegionCords(12L, regions.get(2), 51.543646,5.083237));
			regionCordsRepository.save(new RegionCords(13L, regions.get(2), 51.543826,5.093056));
			regionCordsRepository.save(new RegionCords(14L, regions.get(2), 51.548777,5.104097));
			regionCordsRepository.save(new RegionCords(15L, regions.get(2), 51.552553,5.100244));
			regionCordsRepository.save(new RegionCords(16L, regions.get(2), 51.551239,5.095844));
			regionCordsRepository.save(new RegionCords(17L, regions.get(2), 51.551692,5.095490));
			regionCordsRepository.save(new RegionCords(18L, regions.get(2), 51.551199,5.088973));
			regionCordsRepository.save(new RegionCords(19L, regions.get(2), 51.549257,5.088222));
			regionCordsRepository.save(new RegionCords(20L, regions.get(2), 51.547790,5.085087));
			regionCordsRepository.save(new RegionCords(21L, regions.get(3), 51.568134,5.075008));
			regionCordsRepository.save(new RegionCords(22L, regions.get(3), 51.567214,5.076759));
			regionCordsRepository.save(new RegionCords(23L, regions.get(3), 51.566974,5.079464));
			regionCordsRepository.save(new RegionCords(24L, regions.get(3), 51.566994,5.081139));
			regionCordsRepository.save(new RegionCords(25L, regions.get(3), 51.567601,5.083669));
			regionCordsRepository.save(new RegionCords(26L, regions.get(3), 51.567787,5.087083));
			regionCordsRepository.save(new RegionCords(27L, regions.get(3), 51.572955,5.084623));
			regionCordsRepository.save(new RegionCords(28L, regions.get(3), 51.576863,5.084248));
			regionCordsRepository.save(new RegionCords(29L, regions.get(3), 51.576569,5.077360));
			regionCordsRepository.save(new RegionCords(30L, regions.get(3), 51.571602,5.079183));
			regionCordsRepository.save(new RegionCords(31L, regions.get(3), 51.569988,5.076500));
			regionCordsRepository.save(new RegionCords(32L, regions.get(4), 51.558593,5.038680));
			regionCordsRepository.save(new RegionCords(33L, regions.get(4), 51.557992,5.041299));
			regionCordsRepository.save(new RegionCords(34L, regions.get(4), 51.556227,5.063909));
			regionCordsRepository.save(new RegionCords(35L, regions.get(4), 51.562992,5.065267));
			regionCordsRepository.save(new RegionCords(36L, regions.get(4), 51.566360,5.042596));
			regionCordsRepository.save(new RegionCords(37L, regions.get(5), 51.551159,5.088699));
			regionCordsRepository.save(new RegionCords(38L, regions.get(5), 51.551652,5.095546));
			regionCordsRepository.save(new RegionCords(39L, regions.get(5), 51.551265,5.095932));
			regionCordsRepository.save(new RegionCords(40L, regions.get(5), 51.555268,5.106349));
			regionCordsRepository.save(new RegionCords(41L, regions.get(5), 51.561238,5.103719));
			regionCordsRepository.save(new RegionCords(42L, regions.get(5), 51.559981,5.096958));
			regionCordsRepository.save(new RegionCords(43L, regions.get(5), 51.559997,5.091738));
			regionCordsRepository.save(new RegionCords(44L, regions.get(5), 51.557203,5.093050));
			regionCordsRepository.save(new RegionCords(45L, regions.get(5), 51.552596,5.090554));
			regionCordsRepository.save(new RegionCords(46L, regions.get(5), 51.552343,5.088353));
			regionCordsRepository.save(new RegionCords(47L, regions.get(6), 51.544030,5.077530));
			regionCordsRepository.save(new RegionCords(48L, regions.get(6), 51.543643,5.083370));
			regionCordsRepository.save(new RegionCords(49L, regions.get(6), 51.547863,5.085072));
			regionCordsRepository.save(new RegionCords(50L, regions.get(6), 51.549244,5.088190));
			regionCordsRepository.save(new RegionCords(51L, regions.get(6), 51.551149,5.088720));
			regionCordsRepository.save(new RegionCords(52L, regions.get(6), 51.552373,5.088312));
			regionCordsRepository.save(new RegionCords(53L, regions.get(6), 51.551205,5.084637));
			regionCordsRepository.save(new RegionCords(54L, regions.get(6), 51.550825,5.078157));
			regionCordsRepository.save(new RegionCords(55L, regions.get(6), 51.549024,5.076838));
			regionCordsRepository.save(new RegionCords(56L, regions.get(7), 51.562105,5.109362));
			regionCordsRepository.save(new RegionCords(57L, regions.get(7), 51.545414,5.119246));
			regionCordsRepository.save(new RegionCords(58L, regions.get(7), 51.547149,5.119955));
			regionCordsRepository.save(new RegionCords(59L, regions.get(7), 51.550565,5.124869));
			regionCordsRepository.save(new RegionCords(60L, regions.get(7), 51.554408,5.121734));
			regionCordsRepository.save(new RegionCords(61L, regions.get(7), 51.557356,5.124075));
			regionCordsRepository.save(new RegionCords(62L, regions.get(7), 51.559584,5.128562));
			regionCordsRepository.save(new RegionCords(63L, regions.get(7), 51.561486,5.127188));
			regionCordsRepository.save(new RegionCords(64L, regions.get(7), 51.566635,5.127167));
			regionCordsRepository.save(new RegionCords(65L, regions.get(8), 51.566414,5.042479));
			regionCordsRepository.save(new RegionCords(66L, regions.get(8), 51.563599,5.061804));
			regionCordsRepository.save(new RegionCords(67L, regions.get(8), 51.568648,5.062575));
			regionCordsRepository.save(new RegionCords(68L, regions.get(8), 51.572089,5.061513));
			regionCordsRepository.save(new RegionCords(69L, regions.get(8), 51.575569,5.063059));
			regionCordsRepository.save(new RegionCords(70L, regions.get(8), 51.577263,5.047937));
			regionCordsRepository.save(new RegionCords(71L, regions.get(8), 51.576810,5.046724));
			regionCordsRepository.save(new RegionCords(72L, regions.get(9), 51.574229,5.096820));
			regionCordsRepository.save(new RegionCords(73L, regions.get(9), 51.570822,5.100910));
			regionCordsRepository.save(new RegionCords(74L, regions.get(9), 51.570115,5.114558));
			regionCordsRepository.save(new RegionCords(75L, regions.get(9), 51.570662,5.115138));
			regionCordsRepository.save(new RegionCords(76L, regions.get(9), 51.570008,5.117779));
			regionCordsRepository.save(new RegionCords(77L, regions.get(9), 51.577310,5.124020));
			regionCordsRepository.save(new RegionCords(78L, regions.get(9), 51.583383,5.110961));
			regionCordsRepository.save(new RegionCords(79L, regions.get(9), 51.583943,5.108631));
			regionCordsRepository.save(new RegionCords(80L, regions.get(9), 51.581097,5.106248));
			regionCordsRepository.save(new RegionCords(81L, regions.get(9), 51.580577,5.104154));
			regionCordsRepository.save(new RegionCords(82L, regions.get(9), 51.579416,5.102866));
			regionCordsRepository.save(new RegionCords(83L, regions.get(9), 51.576769,5.104391));
			regionCordsRepository.save(new RegionCords(84L, regions.get(9), 51.574009,5.100522));
			regionCordsRepository.save(new RegionCords(85L, regions.get(10), 51.536926,5.076172));
			regionCordsRepository.save(new RegionCords(86L, regions.get(10), 51.535345,5.094364));
			regionCordsRepository.save(new RegionCords(87L, regions.get(10), 51.531439,5.100137));
			regionCordsRepository.save(new RegionCords(88L, regions.get(10), 51.536598,5.108077));
			regionCordsRepository.save(new RegionCords(89L, regions.get(10), 51.540869,5.113508));
			regionCordsRepository.save(new RegionCords(90L, regions.get(10), 51.542711,5.113401));
			regionCordsRepository.save(new RegionCords(91L, regions.get(10), 51.545106,5.111769));
			regionCordsRepository.save(new RegionCords(92L, regions.get(10), 51.551631,5.110019));
			regionCordsRepository.save(new RegionCords(93L, regions.get(10), 51.543885,5.093016));
			regionCordsRepository.save(new RegionCords(94L, regions.get(10), 51.543605,5.083411));
			regionCordsRepository.save(new RegionCords(95L, regions.get(10), 51.544012,5.077488));
			regionCordsRepository.save(new RegionCords(96L, regions.get(11), 51.583977,4.985841));
			regionCordsRepository.save(new RegionCords(97L, regions.get(11), 51.582597,4.996380));
			regionCordsRepository.save(new RegionCords(98L, regions.get(11), 51.585917,4.997504));
			regionCordsRepository.save(new RegionCords(99L, regions.get(11), 51.584410,5.007150));
			regionCordsRepository.save(new RegionCords(100L, regions.get(11), 51.585917,5.009339));
			regionCordsRepository.save(new RegionCords(101L, regions.get(11), 51.588150,5.005409));
			regionCordsRepository.save(new RegionCords(102L, regions.get(11), 51.591109,5.005992));
			regionCordsRepository.save(new RegionCords(103L, regions.get(11), 51.612307,4.949632));
			regionCordsRepository.save(new RegionCords(104L, regions.get(11), 51.611448,4.948795));
			regionCordsRepository.save(new RegionCords(105L, regions.get(11), 51.603059,4.967818));
			regionCordsRepository.save(new RegionCords(106L, regions.get(11), 51.601819,4.968806));
			regionCordsRepository.save(new RegionCords(107L, regions.get(11), 51.598061,4.967582));
			regionCordsRepository.save(new RegionCords(108L, regions.get(11), 51.597208,4.975675));
			regionCordsRepository.save(new RegionCords(109L, regions.get(11), 51.596235,4.977636));
			regionCordsRepository.save(new RegionCords(110L, regions.get(11), 51.594022,4.977936));
			regionCordsRepository.save(new RegionCords(111L, regions.get(12), 51.561218,5.103763));
			regionCordsRepository.save(new RegionCords(112L, regions.get(12), 51.555248,5.106274));
			regionCordsRepository.save(new RegionCords(113L, regions.get(12), 51.557116,5.111660));
			regionCordsRepository.save(new RegionCords(114L, regions.get(12), 51.562285,5.108731));
			regionCordsRepository.save(new RegionCords(115L, regions.get(13), 51.578003,5.073643));
			regionCordsRepository.save(new RegionCords(116L, regions.get(13), 51.579010,5.083002));
			regionCordsRepository.save(new RegionCords(117L, regions.get(13), 51.578770,5.088914));
			regionCordsRepository.save(new RegionCords(118L, regions.get(13), 51.578203,5.090933));
			regionCordsRepository.save(new RegionCords(119L, regions.get(13), 51.581230,5.093807));
			regionCordsRepository.save(new RegionCords(120L, regions.get(13), 51.581637,5.082314));
			regionCordsRepository.save(new RegionCords(121L, regions.get(13), 51.582790,5.076392));
			regionCordsRepository.save(new RegionCords(122L, regions.get(14), 51.545554,5.054078));
			regionCordsRepository.save(new RegionCords(123L, regions.get(14), 51.544433,5.054700));
			regionCordsRepository.save(new RegionCords(124L, regions.get(14), 51.543960,5.056236));
			regionCordsRepository.save(new RegionCords(125L, regions.get(14), 51.544046,5.077574));
			regionCordsRepository.save(new RegionCords(126L, regions.get(14), 51.549024,5.076855));
			regionCordsRepository.save(new RegionCords(127L, regions.get(14), 51.550718,5.078176));
			regionCordsRepository.save(new RegionCords(128L, regions.get(14), 51.553093,5.079184));
			regionCordsRepository.save(new RegionCords(129L, regions.get(14), 51.555088,5.077746));
			regionCordsRepository.save(new RegionCords(130L, regions.get(14), 51.553667,5.072059));
			regionCordsRepository.save(new RegionCords(131L, regions.get(14), 51.553147,5.064694));
			regionCordsRepository.save(new RegionCords(132L, regions.get(14), 51.554701,5.063664));
			regionCordsRepository.save(new RegionCords(133L, regions.get(14), 51.550518,5.060794));
			regionCordsRepository.save(new RegionCords(134L, regions.get(14), 51.547276,5.056900));
			regionCordsRepository.save(new RegionCords(135L, regions.get(15), 51.558026,5.041310));
			regionCordsRepository.save(new RegionCords(136L, regions.get(15), 51.547288,5.056807));
			regionCordsRepository.save(new RegionCords(137L, regions.get(15), 51.550597,5.060732));
			regionCordsRepository.save(new RegionCords(138L, regions.get(15), 51.554680,5.063565));
			regionCordsRepository.save(new RegionCords(139L, regions.get(15), 51.554680,5.063565));
			regionCordsRepository.save(new RegionCords(140L, regions.get(16), 51.554890,5.106476));
			regionCordsRepository.save(new RegionCords(141L, regions.get(16), 51.551731,5.110063));
			regionCordsRepository.save(new RegionCords(142L, regions.get(16), 51.552812,5.114336));
			regionCordsRepository.save(new RegionCords(143L, regions.get(16), 51.555867,5.112553));
			regionCordsRepository.save(new RegionCords(144L, regions.get(16), 51.556614,5.111415));
			regionCordsRepository.save(new RegionCords(145L, regions.get(17), 51.540207,5.033542));
			regionCordsRepository.save(new RegionCords(146L, regions.get(17), 51.539673,5.048229));
			regionCordsRepository.save(new RegionCords(147L, regions.get(17), 51.542129,5.049560));
			regionCordsRepository.save(new RegionCords(148L, regions.get(17), 51.547200,5.056903));
			regionCordsRepository.save(new RegionCords(149L, regions.get(17), 51.557980,5.041358));
			regionCordsRepository.save(new RegionCords(150L, regions.get(17), 51.558461,5.039597));
			regionCordsRepository.save(new RegionCords(151L, regions.get(17), 51.553626,5.037923));
			regionCordsRepository.save(new RegionCords(152L, regions.get(17), 51.553626,5.036527));
			regionCordsRepository.save(new RegionCords(153L, regions.get(17), 51.549730,5.036935));
			regionCordsRepository.save(new RegionCords(154L, regions.get(17), 51.546191,5.036085));
			regionCordsRepository.save(new RegionCords(155L, regions.get(17), 51.544379,5.029532));
			regionCordsRepository.save(new RegionCords(156L, regions.get(18), 51.567813,5.087091));
			regionCordsRepository.save(new RegionCords(157L, regions.get(18), 51.567224,5.090117));
			regionCordsRepository.save(new RegionCords(158L, regions.get(18), 51.567425,5.094465));
			regionCordsRepository.save(new RegionCords(159L, regions.get(18), 51.567565,5.095185));
			regionCordsRepository.save(new RegionCords(160L, regions.get(18), 51.568432,5.096671));
			regionCordsRepository.save(new RegionCords(161L, regions.get(18), 51.568478,5.098206));
			regionCordsRepository.save(new RegionCords(162L, regions.get(18), 51.568892,5.098195));
			regionCordsRepository.save(new RegionCords(163L, regions.get(18), 51.575533,5.090148));
			regionCordsRepository.save(new RegionCords(164L, regions.get(18), 51.574460,5.086069));
			regionCordsRepository.save(new RegionCords(165L, regions.get(18), 51.573593,5.086004));
			regionCordsRepository.save(new RegionCords(166L, regions.get(18), 51.572946,5.084544));
			regionCordsRepository.save(new RegionCords(167L, regions.get(19), 51.569481,4.967838));
			regionCordsRepository.save(new RegionCords(168L, regions.get(19), 51.564993,4.993264));
			regionCordsRepository.save(new RegionCords(169L, regions.get(19), 51.573769,4.993396));
			regionCordsRepository.save(new RegionCords(170L, regions.get(19), 51.577283,4.970465));
			regionCordsRepository.save(new RegionCords(171L, regions.get(19), 51.577343,4.969262));
			regionCordsRepository.save(new RegionCords(172L, regions.get(19), 51.573629,4.967012));
			regionCordsRepository.save(new RegionCords(173L, regions.get(19), 51.571308,4.968920));
			regionCordsRepository.save(new RegionCords(174L, regions.get(20), 51.565066,4.993174));
			regionCordsRepository.save(new RegionCords(175L, regions.get(20), 51.561518,5.018382));
			regionCordsRepository.save(new RegionCords(176L, regions.get(20), 51.569948,5.018038));
			regionCordsRepository.save(new RegionCords(177L, regions.get(20), 51.573762,4.993417));
			regionCordsRepository.save(new RegionCords(178L, regions.get(21), 51.573039,5.084627));
			regionCordsRepository.save(new RegionCords(179L, regions.get(21), 51.573466,5.085835));
			regionCordsRepository.save(new RegionCords(180L, regions.get(21), 51.574516,5.086173));
			regionCordsRepository.save(new RegionCords(181L, regions.get(21), 51.575606,5.089981));
			regionCordsRepository.save(new RegionCords(182L, regions.get(21), 51.576866,5.084213));
			regionCordsRepository.save(new RegionCords(183L, regions.get(22), 51.578850,5.066488));
			regionCordsRepository.save(new RegionCords(184L, regions.get(22), 51.578096,5.073415));
			regionCordsRepository.save(new RegionCords(185L, regions.get(22), 51.582830,5.076271));
			regionCordsRepository.save(new RegionCords(186L, regions.get(22), 51.583843,5.067980));

			regionCordsRepository.save(new RegionCords(187L, regions.get(23), 51.553087,5.064632));
			regionCordsRepository.save(new RegionCords(188L, regions.get(23), 51.553774,5.072124));
			regionCordsRepository.save(new RegionCords(189L, regions.get(23), 51.554995,5.077640));
			regionCordsRepository.save(new RegionCords(190L, regions.get(23), 51.555301,5.077790));
			regionCordsRepository.save(new RegionCords(191L, regions.get(23), 51.556282,5.064034));
			regionCordsRepository.save(new RegionCords(192L, regions.get(23), 51.554788,5.063540));

			regionCordsRepository.save(new RegionCords(193L, regions.get(24), 51.561571,5.018270));
			regionCordsRepository.save(new RegionCords(194L, regions.get(24), 51.558717,5.038783));
			regionCordsRepository.save(new RegionCords(195L, regions.get(24), 51.566400,5.042620));
			regionCordsRepository.save(new RegionCords(196L, regions.get(24), 51.570081,5.017966));
			regionCordsRepository.save(new RegionCords(197L, regions.get(25), 51.570135,5.018057));
			regionCordsRepository.save(new RegionCords(198L, regions.get(25), 51.566440,5.042473));
			regionCordsRepository.save(new RegionCords(199L, regions.get(25), 51.572495,5.045110));
			regionCordsRepository.save(new RegionCords(200L, regions.get(25), 51.575036,5.037457));
			regionCordsRepository.save(new RegionCords(201L, regions.get(25), 51.576349,5.029238));
			regionCordsRepository.save(new RegionCords(202L, regions.get(25), 51.578750,5.022781));
			regionCordsRepository.save(new RegionCords(203L, regions.get(26), 51.542679,5.113674));
			regionCordsRepository.save(new RegionCords(204L, regions.get(26), 51.545748,5.118226));
			regionCordsRepository.save(new RegionCords(205L, regions.get(26), 51.552746,5.114342));
			regionCordsRepository.save(new RegionCords(206L, regions.get(26), 51.551686,5.109911));
			regionCordsRepository.save(new RegionCords(207L, regions.get(26), 51.545414,5.111499));
			regionCordsRepository.save(new RegionCords(208L, regions.get(27), 51.569004,5.065873));
			regionCordsRepository.save(new RegionCords(209L, regions.get(27), 51.568691,5.066163));
			regionCordsRepository.save(new RegionCords(210L, regions.get(27), 51.567914,5.069870));
			regionCordsRepository.save(new RegionCords(211L, regions.get(27), 51.568097,5.072286));
			regionCordsRepository.save(new RegionCords(212L, regions.get(27), 51.568578,5.073381));
			regionCordsRepository.save(new RegionCords(213L, regions.get(27), 51.568574,5.074194));
			regionCordsRepository.save(new RegionCords(214L, regions.get(27), 51.568204,5.074929));
			regionCordsRepository.save(new RegionCords(215L, regions.get(27), 51.569948,5.076409));
			regionCordsRepository.save(new RegionCords(216L, regions.get(27), 51.571635,5.079118));
			regionCordsRepository.save(new RegionCords(217L, regions.get(27), 51.576533,5.077460));
			regionCordsRepository.save(new RegionCords(218L, regions.get(27), 51.575553,5.066517));
			regionCordsRepository.save(new RegionCords(219L, regions.get(28), 51.563592,5.061890));
			regionCordsRepository.save(new RegionCords(220L, regions.get(28), 51.563039,5.065325));
			regionCordsRepository.save(new RegionCords(221L, regions.get(28), 51.568608,5.066331));
			regionCordsRepository.save(new RegionCords(222L, regions.get(28), 51.568901,5.065901));
			regionCordsRepository.save(new RegionCords(223L, regions.get(28), 51.578503,5.066416));
			regionCordsRepository.save(new RegionCords(224L, regions.get(28), 51.580803,5.045192));
			regionCordsRepository.save(new RegionCords(225L, regions.get(28), 51.577276,5.048113));
			regionCordsRepository.save(new RegionCords(226L, regions.get(28), 51.575576,5.063068));
			regionCordsRepository.save(new RegionCords(227L, regions.get(28), 51.572129,5.061491));
			regionCordsRepository.save(new RegionCords(228L, regions.get(28), 51.568748,5.062640));
			regionCordsRepository.save(new RegionCords(229L, regions.get(29), 51.578103,5.073443));
			regionCordsRepository.save(new RegionCords(230L, regions.get(29), 51.579023,5.082869));
			regionCordsRepository.save(new RegionCords(231L, regions.get(29), 51.579143,5.085951));
			regionCordsRepository.save(new RegionCords(232L, regions.get(29), 51.578730,5.089032));
			regionCordsRepository.save(new RegionCords(233L, regions.get(29), 51.578190,5.090932));
			regionCordsRepository.save(new RegionCords(234L, regions.get(29), 51.581203,5.093862));
			regionCordsRepository.save(new RegionCords(235L, regions.get(29), 51.590596,5.096178));
			regionCordsRepository.save(new RegionCords(236L, regions.get(29), 51.594109,5.081838));
			regionCordsRepository.save(new RegionCords(237L, regions.get(29), 51.592676,5.082214));
			regionCordsRepository.save(new RegionCords(238L, regions.get(29), 51.587223,5.079796));
			regionCordsRepository.save(new RegionCords(239L, regions.get(29), 51.582817,5.076330));
			regionCordsRepository.save(new RegionCords(240L, regions.get(30), 51.577246,4.970286));
			regionCordsRepository.save(new RegionCords(241L, regions.get(30), 51.574925,4.986862));
			regionCordsRepository.save(new RegionCords(242L, regions.get(30), 51.580931,4.987328));
			regionCordsRepository.save(new RegionCords(243L, regions.get(30), 51.588411,4.984471));
			regionCordsRepository.save(new RegionCords(244L, regions.get(30), 51.593810,4.978094));
			regionCordsRepository.save(new RegionCords(245L, regions.get(30), 51.596129,4.977643));
			regionCordsRepository.save(new RegionCords(246L, regions.get(30), 51.597196,4.975711));
			regionCordsRepository.save(new RegionCords(247L, regions.get(30), 51.597329,4.972946));
			regionCordsRepository.save(new RegionCords(248L, regions.get(30), 51.592104,4.973483));
			regionCordsRepository.save(new RegionCords(249L, regions.get(31), 51.539520,5.048103));
			regionCordsRepository.save(new RegionCords(250L, regions.get(31), 51.536857,5.076223));
			regionCordsRepository.save(new RegionCords(251L, regions.get(31), 51.544017,5.077542));
			regionCordsRepository.save(new RegionCords(252L, regions.get(31), 51.543950,5.056516));
			regionCordsRepository.save(new RegionCords(253L, regions.get(31), 51.544591,5.054509));
			regionCordsRepository.save(new RegionCords(254L, regions.get(31), 51.545672,5.053875));
			regionCordsRepository.save(new RegionCords(255L, regions.get(31), 51.542276,5.049163));

			regionCordsRepository.save(new RegionCords(267L, regions.get(32), 51.582777,5.076349));
			regionCordsRepository.save(new RegionCords(268L, regions.get(32), 51.581590,5.082305));
			regionCordsRepository.save(new RegionCords(269L, regions.get(32), 51.587523,5.084923));
			regionCordsRepository.save(new RegionCords(270L, regions.get(32), 51.588276,5.086469));
			regionCordsRepository.save(new RegionCords(271L, regions.get(32), 51.587883,5.089014));
			regionCordsRepository.save(new RegionCords(272L, regions.get(32), 51.592049,5.090171));
			regionCordsRepository.save(new RegionCords(273L, regions.get(32), 51.594109,5.081811));
			regionCordsRepository.save(new RegionCords(274L, regions.get(32), 51.592622,5.082134));
			regionCordsRepository.save(new RegionCords(275L, regions.get(32), 51.587216,5.079773));

			regionCordsRepository.save(new RegionCords(276L, regions.get(33), 51.578830,5.022727));
			regionCordsRepository.save(new RegionCords(277L, regions.get(33), 51.576269,5.029168));
			regionCordsRepository.save(new RegionCords(278L, regions.get(33), 51.575009,5.037397));
			regionCordsRepository.save(new RegionCords(279L, regions.get(33), 51.572482,5.045057));
			regionCordsRepository.save(new RegionCords(280L, regions.get(33), 51.576743,5.046890));
			regionCordsRepository.save(new RegionCords(281L, regions.get(33), 51.577270,5.048028));
			regionCordsRepository.save(new RegionCords(282L, regions.get(33), 51.580850,5.044936));
			regionCordsRepository.save(new RegionCords(283L, regions.get(33), 51.582930,5.028005));

			regionCordsRepository.save(new RegionCords(284L, regions.get(34), 51.583870,5.067962));
			regionCordsRepository.save(new RegionCords(285L, regions.get(34), 51.582810,5.076385));
			regionCordsRepository.save(new RegionCords(286L, regions.get(34), 51.587576,5.079978));
			regionCordsRepository.save(new RegionCords(287L, regions.get(34), 51.592802,5.082220));
			regionCordsRepository.save(new RegionCords(288L, regions.get(34), 51.594069,5.081855));
			regionCordsRepository.save(new RegionCords(289L, regions.get(34), 51.596341,5.071626));
			regionCordsRepository.save(new RegionCords(290L, regions.get(34), 51.591416,5.070508));

			regionCordsRepository.save(new RegionCords(291L, regions.get(35), 51.572149,5.005452));
			regionCordsRepository.save(new RegionCords(292L, regions.get(35), 51.570095,5.017970));
			regionCordsRepository.save(new RegionCords(293L, regions.get(35), 51.578776,5.022796));
			regionCordsRepository.save(new RegionCords(294L, regions.get(35), 51.580597,5.017094));
			regionCordsRepository.save(new RegionCords(295L, regions.get(35), 51.583617,5.011630));
			regionCordsRepository.save(new RegionCords(296L, regions.get(35), 51.584423,5.007131));
			regionCordsRepository.save(new RegionCords(297L, regions.get(35), 51.579276,5.005272));
			regionCordsRepository.save(new RegionCords(298L, regions.get(35), 51.576763,5.007129));

			regionCordsRepository.save(new RegionCords(299L, regions.get(36), 51.580170,4.995583));
			regionCordsRepository.save(new RegionCords(300L, regions.get(36), 51.578143,5.002161));
			regionCordsRepository.save(new RegionCords(301L, regions.get(36), 51.579236,5.005146));
			regionCordsRepository.save(new RegionCords(302L, regions.get(36), 51.584463,5.007087));
			regionCordsRepository.save(new RegionCords(303L, regions.get(36), 51.585903,4.997417));

			regionCordsRepository.save(new RegionCords(304L, regions.get(37), 51.562399,5.109120));
			regionCordsRepository.save(new RegionCords(305L, regions.get(37), 51.562919,5.111632));
			regionCordsRepository.save(new RegionCords(306L, regions.get(37), 51.570028,5.117786));
			regionCordsRepository.save(new RegionCords(307L, regions.get(37), 51.570675,5.115145));
			regionCordsRepository.save(new RegionCords(308L, regions.get(37), 51.570181,5.114715));
			regionCordsRepository.save(new RegionCords(309L, regions.get(37), 51.570822,5.100909));
			regionCordsRepository.save(new RegionCords(310L, regions.get(37), 51.565853,5.106810));

			regionCordsRepository.save(new RegionCords(311L, regions.get(38), 51.573749,4.993533));
			regionCordsRepository.save(new RegionCords(312L, regions.get(38), 51.572129,5.005436));
			regionCordsRepository.save(new RegionCords(313L, regions.get(38), 51.576749,5.007107));
			regionCordsRepository.save(new RegionCords(314L, regions.get(38), 51.577263,5.005069));
			regionCordsRepository.save(new RegionCords(315L, regions.get(38), 51.576743,5.002546));
			regionCordsRepository.save(new RegionCords(316L, regions.get(38), 51.578130,5.002235));
			regionCordsRepository.save(new RegionCords(317L, regions.get(38), 51.580077,4.995529));
			regionCordsRepository.save(new RegionCords(318L, regions.get(39), 51.560031,5.092040));
			regionCordsRepository.save(new RegionCords(319L, regions.get(39), 51.560077,5.096914));
			regionCordsRepository.save(new RegionCords(320L, regions.get(39), 51.561305,5.103678));
			regionCordsRepository.save(new RegionCords(321L, regions.get(39), 51.568448,5.098271));
			regionCordsRepository.save(new RegionCords(322L, regions.get(39), 51.568381,5.096660));
			regionCordsRepository.save(new RegionCords(323L, regions.get(39), 51.567534,5.095050));
			regionCordsRepository.save(new RegionCords(324L, regions.get(39), 51.567441,5.094449));
			regionCordsRepository.save(new RegionCords(325L, regions.get(39), 51.564953,5.094975));
			regionCordsRepository.save(new RegionCords(326L, regions.get(40), 51.574809,4.986881));
			regionCordsRepository.save(new RegionCords(327L, regions.get(40), 51.573829,4.993423));
			regionCordsRepository.save(new RegionCords(328L, regions.get(40), 51.582497,4.996515));
			regionCordsRepository.save(new RegionCords(329L, regions.get(40), 51.583950,4.985801));
			regionCordsRepository.save(new RegionCords(330L, regions.get(40), 51.581110,4.987390));
			regionCordsRepository.save(new RegionCords(331L, regions.get(41), 51.578770,5.022783));
			regionCordsRepository.save(new RegionCords(332L, regions.get(41), 51.582870,5.027837));
			regionCordsRepository.save(new RegionCords(333L, regions.get(41), 51.591103,5.006005));
			regionCordsRepository.save(new RegionCords(334L, regions.get(41), 51.588063,5.005401));
			regionCordsRepository.save(new RegionCords(335L, regions.get(41), 51.585883,5.009296));
			regionCordsRepository.save(new RegionCords(336L, regions.get(41), 51.584503,5.007128));
			regionCordsRepository.save(new RegionCords(337L, regions.get(41), 51.583570,5.011540));
			regionCordsRepository.save(new RegionCords(338L, regions.get(41), 51.580717,5.016572));
			regionCordsRepository.save(new RegionCords(339L, regions.get(42), 51.575576,5.066334));
			regionCordsRepository.save(new RegionCords(340L, regions.get(42), 51.576803,5.084171));
			regionCordsRepository.save(new RegionCords(341L, regions.get(42), 51.575523,5.090173));
			regionCordsRepository.save(new RegionCords(342L, regions.get(42), 51.575763,5.091891));
			regionCordsRepository.save(new RegionCords(343L, regions.get(42), 51.577523,5.090066));
			regionCordsRepository.save(new RegionCords(344L, regions.get(42), 51.577923,5.090410));
			regionCordsRepository.save(new RegionCords(345L, regions.get(42), 51.578670,5.087425));
			regionCordsRepository.save(new RegionCords(346L, regions.get(42), 51.578616,5.082551));
			regionCordsRepository.save(new RegionCords(347L, regions.get(42), 51.577736,5.074864));
			regionCordsRepository.save(new RegionCords(348L, regions.get(42), 51.578496,5.066297));
			regionCordsRepository.save(new RegionCords(349L, regions.get(43), 51.576753,5.002546));
			regionCordsRepository.save(new RegionCords(350L, regions.get(43), 51.577290,5.005081));
			regionCordsRepository.save(new RegionCords(351L, regions.get(43), 51.576823,5.007153));
			regionCordsRepository.save(new RegionCords(352L, regions.get(43), 51.579270,5.005186));
			regionCordsRepository.save(new RegionCords(353L, regions.get(43), 51.578276,5.002244));

			regionCordsRepository.save(new RegionCords(256L, regions.get(44), 51.574229,5.096950));
			regionCordsRepository.save(new RegionCords(257L, regions.get(44), 51.574009,5.100450));
			regionCordsRepository.save(new RegionCords(258L, regions.get(44), 51.577130,5.104387));
			regionCordsRepository.save(new RegionCords(259L, regions.get(44), 51.579290,5.102927));
			regionCordsRepository.save(new RegionCords(260L, regions.get(44), 51.581083,5.106233));
			regionCordsRepository.save(new RegionCords(261L, regions.get(44), 51.583803,5.108552));
			regionCordsRepository.save(new RegionCords(262L, regions.get(44), 51.583350,5.110925));
			regionCordsRepository.save(new RegionCords(263L, regions.get(44), 51.590749,5.096413));
			regionCordsRepository.save(new RegionCords(264L, regions.get(44), 51.581103,5.093764));
			regionCordsRepository.save(new RegionCords(265L, regions.get(44), 51.578216,5.090900));
			regionCordsRepository.save(new RegionCords(266L, regions.get(44), 51.577756,5.091028));

			regionCordsRepository.save(new RegionCords(354L, regions.get(45), 51.561278,5.103800));
			regionCordsRepository.save(new RegionCords(355L, regions.get(45), 51.562292,5.108631));
			regionCordsRepository.save(new RegionCords(356L, regions.get(45), 51.565306,5.106610));
			regionCordsRepository.save(new RegionCords(357L, regions.get(45), 51.570268,5.100514));
			regionCordsRepository.save(new RegionCords(358L, regions.get(45), 51.576603,5.093152));
			regionCordsRepository.save(new RegionCords(359L, regions.get(45), 51.577856,5.090554));
			regionCordsRepository.save(new RegionCords(360L, regions.get(45), 51.577230,5.089845));
			regionCordsRepository.save(new RegionCords(361L, regions.get(45), 51.575869,5.091993));
			regionCordsRepository.save(new RegionCords(362L, regions.get(45), 51.575483,5.090253));
			regionCordsRepository.save(new RegionCords(363L, regions.get(45), 51.572469,5.094227));
			regionCordsRepository.save(new RegionCords(364L, regions.get(45), 51.568348,5.098736));
		};
	}
}

