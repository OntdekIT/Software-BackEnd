package Ontdekstation013.ClimateChecker.features.user.authentication;

import Ontdekstation013.ClimateChecker.config.JwtService;
import Ontdekstation013.ClimateChecker.features.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;

    public String authenticate(User user) {
        return jwtService.generateToken(user);
    }
}
