package Ontdekstation013.ClimateChecker.features.user.authentication;

import Ontdekstation013.ClimateChecker.features.user.User;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;

@Service
public class AuthenticationService {
    public ResponseCookie createCookie(User user) {
        Cookie jwtTokenCookie = new Cookie("user-id", user.getUserId().toString());
        ResponseCookie springCookie = ResponseCookie.from(jwtTokenCookie.getName(), jwtTokenCookie.getValue())
                .httpOnly(true)
                .sameSite("None")
                .path("/")
                .build();

        return springCookie;
    }
}
