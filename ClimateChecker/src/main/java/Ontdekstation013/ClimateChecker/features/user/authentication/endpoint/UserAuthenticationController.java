package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.user.PasswordEncodingService;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.authentication.*;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.*;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/authentication")
public class UserAuthenticationController {
    private final AuthenticationService authService;
    private final UserService userService;
    private final TokenService tokenService;
    private final EmailSenderService emailSenderService;
    private final WorkshopService workshopService;
    private final StationService stationService;
    private final PasswordEncodingService passwordEncodingService;

    @PostMapping("register")
    public ResponseEntity<?> createNewUser(@RequestBody RegisterUserRequest registerRequest) throws MessagingException {
        try {
            if (!workshopService.verifyWorkshopCode(registerRequest.workshopCode())) {
                throw new InvalidArgumentException("Ongeldige workshopcode");
            }

            Station station = stationService.getByRegistrationCode(registerRequest.stationCode());

            if (station == null) {
                throw new InvalidArgumentException("Ongeldige stationcode");
            } else if (station.getUserid() != null) {
                throw new InvalidArgumentException("Station is al geclaimd");
            }

            Workshop workshop = workshopService.getByCode(registerRequest.workshopCode());
            User user = UserMapper.toUser(
                    registerRequest,
                    passwordEncodingService.encodePassword(registerRequest.password()),
                    workshop
            );

            user = userService.createNewUser(user);
            station.setUserid(user.getUserId());
            stationService.UpdateMeetstation(station);
            Token token = tokenService.createVerifyToken(user.getUserId(), TokenType.VERIFY_AUTH);
            emailSenderService.sendSignupMail(user.getEmail(), user.getFirstName(), user.getLastName(), token.getNumericCode());
            UserResponse userResponse = UserMapper.toUserResponse(user, false);

            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (InvalidArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een onverwachte fout opgetreden.");
        }
    }



    @PostMapping("login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) throws Exception {
        User user = userService.getUserByEmail(loginRequest.email());
        if (user != null && passwordEncodingService.verifyPassword(loginRequest.password(), user.getPassword())) {
            Token token = tokenService.createVerifyToken(user.getUserId(), TokenType.VERIFY_AUTH);
            emailSenderService.sendLoginMail(user.getEmail(), user.getFirstName(), user.getLastName(), token.getNumericCode());
        } else {
            throw new InvalidArgumentException("Invalid email and/or password");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("verify")
    public ResponseEntity<AuthenticationResponse> verifyEmailCode(@RequestBody VerifyLoginRequest verifyLoginRequest) {
        ResponseEntity<AuthenticationResponse> responseEntity = ResponseEntity.badRequest().build();
        User user = userService.getUserByEmail(verifyLoginRequest.email());

        if (user != null && tokenService.verifyToken(verifyLoginRequest.code(), user.getUserId(), TokenType.VERIFY_AUTH)) {
            String token = authService.authenticate(user);
            responseEntity = ResponseEntity.ok(new AuthenticationResponse(token));
        }

        return responseEntity;
    }

    @PostMapping("reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        User user = userService.getUserByEmail(resetPasswordRequest.email());
        if (user != null && tokenService.verifyToken(resetPasswordRequest.token(), user.getUserId(), TokenType.PASSWORD_RESET)) {
            user.setPassword(passwordEncodingService.encodePassword(resetPasswordRequest.password()));
            userService.updateUser(user.getUserId(), user);
        } else {
            throw new InvalidArgumentException("Invalid email and/or token");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("forgot-password")
    public ResponseEntity<?> createForgotPasswordRequest(@RequestBody ForgotPasswordRequest forgotPasswordRequest) throws Exception {
        User user = userService.getUserByEmail(forgotPasswordRequest.email());

        if (user != null) {
            Token token = tokenService.createVerifyToken(user.getUserId(), TokenType.PASSWORD_RESET);
            emailSenderService.sendForgotPasswordMail(user.getEmail(), user.getFirstName(), user.getLastName(), token.getNumericCode());
        } else {
            throw new InvalidArgumentException("Invalid email");
        }

        return ResponseEntity.ok().build();
    }

    // Not necessary when using JWTs
    // May need to be re-added when the auth mechanism is upgraded
    // ---------------------------------------------------------
    //    @PostMapping("logout")
    //    public ResponseEntity<String> clearCookies(HttpServletResponse response, HttpServletRequest request) {
    //        Cookie[] cookies = request.getCookies();
    //
    //        if (cookies != null) {
    //            for (Cookie cookie : cookies) {
    //                cookie.setMaxAge(0);
    //                cookie.setPath("/");
    //                response.addCookie(cookie);
    //            }
    //        }
    //        return ResponseEntity.ok().build();
    //    }
}
