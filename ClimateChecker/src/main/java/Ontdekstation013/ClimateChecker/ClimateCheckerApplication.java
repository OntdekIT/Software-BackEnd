package Ontdekstation013.ClimateChecker;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ClimateCheckerApplication {

	@Value("${application.timezone:UTC}")
	private String applicationTimezone;

	public static void main(String[] args) {
		SpringApplication.run(ClimateCheckerApplication.class, args);
	}

	@PostConstruct
	public void executeAfterMain() {
		TimeZone.setDefault(TimeZone.getTimeZone(applicationTimezone));
	}
}

