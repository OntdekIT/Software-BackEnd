package Ontdekstation013.ClimateChecker.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // Comma-separated list, set per environment via CORS_ALLOWED_ORIGINS
    // (the public URL of the frontend of that environment).
    private final String[] allowedOrigins;

    public CorsConfig(
            @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        // Zodat de frontend de versheid (X-Data-As-Of) en ETag kan lezen.
                        .exposedHeaders("X-Data-As-Of", "ETag")
                        .allowCredentials(true);
            }
        };
    }
}
