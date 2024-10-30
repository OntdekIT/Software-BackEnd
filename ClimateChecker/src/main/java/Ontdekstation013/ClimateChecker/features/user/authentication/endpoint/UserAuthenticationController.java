package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.LoginRequest;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.RegisterRequest;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.VerifyLoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/authentication")
public class UserAuthenticationController {
    private final UserService userService;
    private final EmailSenderService emailSenderService;

    @Autowired
    public UserAuthenticationController(UserService userService, EmailSenderService emailSenderService)
    {
        this.userService = userService;
        this.emailSenderService = emailSenderService;
    }

    @PostMapping("register")
    public ResponseEntity<String> createNewUser(@RequestBody RegisterRequest registerRequest) throws Exception {
        throw new UnsupportedOperationException();
    }

    @PostMapping("login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) throws Exception {
        throw new UnsupportedOperationException();
    }

    @PostMapping("verify")
    public ResponseEntity<Void> verifyEmailCode(@RequestBody VerifyLoginRequest verifyLoginRequest) {
        throw new UnsupportedOperationException();
    }

    @GetMapping("login")
    public ResponseEntity<Boolean> verifyLogin(HttpServletResponse response, HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @DeleteMapping("logout")
    public ResponseEntity<String> clearCookies(HttpServletResponse response, HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }
}
