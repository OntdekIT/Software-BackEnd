package Ontdekstation013.ClimateChecker.config;

import Ontdekstation013.ClimateChecker.features.user.UserRole;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/authentication/**").permitAll()
                        .requestMatchers("/api/users/**").hasAnyAuthority(UserRole.ADMIN.name(), UserRole.SUPER_ADMIN.name())
                        .requestMatchers("/api/workshops/**").hasAnyAuthority(UserRole.ADMIN.name(), UserRole.SUPER_ADMIN.name())
                        // State-changing station operations require an authenticated user.
                        // These must be listed before the blanket /api/Meetstation/** rule,
                        // since request matchers are evaluated top to bottom.
                        .requestMatchers(HttpMethod.PUT, "/api/Meetstation/edit/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/Meetstation/Claim").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/Meetstation/add").authenticated()
                        .requestMatchers("/api/Meetstation/**").permitAll()
                        .requestMatchers("/api/measurement/**").permitAll()
                        .requestMatchers("/api/neighbourhood/**").permitAll()
                        .requestMatchers("/api/e2e/**").permitAll()
                        .requestMatchers("/api/my-account/**").authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

