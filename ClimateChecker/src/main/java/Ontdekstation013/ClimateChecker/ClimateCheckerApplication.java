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


}

