package Ontdekstation013.ClimateChecker.features.user.authentication;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.LoginDto;
import org.springframework.http.ResponseCookie;

import javax.servlet.http.Cookie;

public class AuthenticationService {
    public User login(LoginDto loginDto) throws Exception {
        throw new UnsupportedOperationException();
    }

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
