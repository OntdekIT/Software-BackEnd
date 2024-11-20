package Ontdekstation013.ClimateChecker.config;

//***NOT FOR PRODUCTION ENVIRONMENT, SOLELY FOR TESTING***

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.event.LoggerListener;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public LoggerListener loggerListener() {
        return new LoggerListener();
    }

//    @Override
//    protected void configure(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/api/authentication/register", "/api/authentication/login","/api/authentication/verify", "/api/neighbourhood/history").permitAll()
////                .antMatchers("/api/users/**").hasRole("ADMIN")
//
//                .anyRequest().authenticated()
//                .and()
//                .headers().frameOptions().disable();
//    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeRequests()
                .antMatchers("/", "/console/**", "/api/authentication/login", "/api/authentication/register", "/api/authentication/verify").permitAll()
                .antMatchers("/api/users/**").hasAuthority("ROLE_ADMIN") // Alleen admin-gebruikers hebben toegang
                .anyRequest().authenticated() // Alle andere endpoints vereisen authenticatie
                .and()
                .csrf().disable() // CSRF uitschakelen (alleen voor testomgevingen)
                .headers().frameOptions().disable(); // Nodig voor H2-console, indien van toepassing
    }


}

