package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.user.PasswordUtils;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.authentication.AuthenticationService;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import Ontdekstation013.ClimateChecker.features.user.authentication.Token;
import Ontdekstation013.ClimateChecker.features.user.authentication.TokenService;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.LoginRequest;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.RegisterUserRequest;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.VerifyLoginRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/authentication")
public class UserAuthenticationController {
    private final AuthenticationService authService;
    private final UserService userService;
    private final TokenService tokenService;
    private final EmailSenderService emailSenderService;
    private final WorkshopService workshopService;
    private final StationService stationService;

    @Autowired
    public UserAuthenticationController(AuthenticationService authenticationService, UserService userService, TokenService tokenService, EmailSenderService emailSenderService, WorkshopService workshopService, StationService stationService) {
        this.authService = authenticationService;
        this.userService = userService;
        this.tokenService = tokenService;
        this.emailSenderService = emailSenderService;
        this.workshopService = workshopService;
        this.stationService = stationService;
    }

    @PostMapping("register")
    public ResponseEntity<?> createNewUser(@RequestBody RegisterUserRequest registerRequest) {
        if (!workshopService.VerifyWorkshopCode(registerRequest.workshopCode())) {
            throw new InvalidArgumentException("Invalid workshop code");
        }

        Station station = stationService.getByRegistrationCode(registerRequest.stationCode());

        if (station == null) {
            throw new InvalidArgumentException("Invalid station code");
        } else if (station.getUserid() != null) {
            throw new InvalidArgumentException("Station is already claimed");
        }

        User user = UserMapper.toUser(registerRequest);
        user = userService.createNewUser(user);
        station.setUserid(user.getUserId());
        stationService.UpdateMeetstation(station);

        UserResponse response = UserMapper.toUserResponse(user, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) throws Exception {
        User user = userService.getUserByEmail(loginRequest.email());
        if (user != null && PasswordUtils.verifyPassword(loginRequest.password(), user.getPassword())) {
            Token token = tokenService.createVerifyToken(user.getUserId());
            emailSenderService.sendLoginMail(user.getEmail(), user.getFirstName(), user.getLastName(), token.getNumericCode());
        } else {
            throw new InvalidArgumentException("Invalid email and/or password");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("verify")
    public ResponseEntity<?> verifyEmailCode(@RequestBody VerifyLoginRequest verifyLoginRequest) {
        ResponseEntity<?> responseEntity = ResponseEntity.badRequest().build();
        User user = userService.getUserByEmail(verifyLoginRequest.email());

        if (user != null && tokenService.verifyToken(verifyLoginRequest.code(), verifyLoginRequest.email())) {
            ResponseCookie cookie = authService.createCookie(user);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", cookie.toString() + "; HttpOnly; SameSite=none; Secure");
            responseEntity = ResponseEntity.ok().headers(headers).build();
        }

        return responseEntity;
    }

    @PostMapping("logout")
    public ResponseEntity<String> clearCookies(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        return ResponseEntity.ok().build();
    }
}
