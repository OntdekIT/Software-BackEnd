package Ontdekstation013.ClimateChecker.config;

import Ontdekstation013.ClimateChecker.features.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    /** Alleen true in dev/test (zie application-dev.properties en application-test.properties). */
    @Value("${springdoc.api-docs.enabled:false}")
    private boolean apiDocsEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (apiDocsEnabled) {
            http.authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll());
        }
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/authentication/**").permitAll()
                        .requestMatchers("/api/users/**").hasAnyAuthority(UserRole.ADMIN.name(), UserRole.SUPER_ADMIN.name())
                        .requestMatchers("/api/workshops/**").hasAnyAuthority(UserRole.ADMIN.name(), UserRole.SUPER_ADMIN.name())
                        // State-changing station operations require an authenticated user.
                        // These must be listed before the blanket station rule, since request
                        // matchers are evaluated top to bottom. Every station path exists in its
                        // canonical lowercase form and as the old mixed-case alias (path matching
                        // is case sensitive, so an alias without a rule would bypass these; class and method aliases
                        // combine, hence the four spellings of claim).
                        .requestMatchers(HttpMethod.PUT, "/api/meetstation/edit/**", "/api/Meetstation/edit/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/meetstation/transfer/**", "/api/Meetstation/transfer/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/meetstation/claim", "/api/meetstation/Claim", "/api/Meetstation/claim", "/api/Meetstation/Claim").authenticated()
                        // Adding a station by hand is an admin task, not something any logged-in user may do.
                        .requestMatchers(HttpMethod.POST, "/api/meetstation/add", "/api/Meetstation/add").hasAnyAuthority(UserRole.ADMIN.name(), UserRole.SUPER_ADMIN.name())
                        .requestMatchers("/api/meetstation/**", "/api/Meetstation/**").permitAll()
                        // Coverage pulls weeks of raw data for every station; keep it to admins.
                        .requestMatchers("/api/measurement/coverage").hasAnyAuthority(UserRole.ADMIN.name(), UserRole.SUPER_ADMIN.name())
                        .requestMatchers("/api/measurement/**").permitAll()
                        .requestMatchers("/api/neighbourhood/**").permitAll()
                        .requestMatchers("/api/snapshot", "/api/status").permitAll()
                        .requestMatchers("/api/e2e/**").permitAll()
                        .requestMatchers("/api/my-account/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
