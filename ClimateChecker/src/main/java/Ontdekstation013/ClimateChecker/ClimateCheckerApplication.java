package Ontdekstation013.ClimateChecker;
import Ontdekstation013.ClimateChecker.features.station.StationMonitorService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

@SpringBootApplication
public class ClimateCheckerApplication {

	@Value("${application.timezone:UTC}")
	private String applicationTimezone;

	@Bean
	public CommandLineRunner run(ApplicationContext context) {
		return args -> {
			StationMonitorService stationMonitor = context.getBean(StationMonitorService.class);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ClimateCheckerApplication.class, args);
	}

	@PostConstruct
	public void executeAfterMain() {
		TimeZone.setDefault(TimeZone.getTimeZone(applicationTimezone));
	}
}

