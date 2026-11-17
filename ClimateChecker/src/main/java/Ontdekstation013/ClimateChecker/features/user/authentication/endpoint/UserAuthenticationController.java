package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

import Ontdekstation013.ClimateChecker.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.TooManyAttemptsException;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.user.PasswordEncodingService;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.authentication.*;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.*;
import Ontdekstation013.ClimateChecker.features.user.authentication.trustedip.TrustedIpService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import Ontdekstation013.ClimateChecker.features.workshop.WorkshopService;
import Ontdekstation013.ClimateChecker.utility.RateLimiter;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.NonUniqueResultException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.mail.MailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@Tag(name = "Authenticatie", description = "Registreren, inloggen en wachtwoord herstellen (publiek)")
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
    private final RateLimiter rateLimiter;
    private final TrustedIpService trustedIpService;

    private static final String RATE_LIMIT_MESSAGE = "Te veel pogingen. Probeer het over een minuut opnieuw.";

    private void throwWhenRateLimited(String action, HttpServletRequest request, String email) {
        String key = action + ":" + clientIp(request) + ":" + (email == null ? "" : email.toLowerCase());
        if (!rateLimiter.isAllowed(key)) {
            throw new TooManyAttemptsException();
        }
    }

    /**
     * Bepaalt het client-IP. Achter een reverse proxy/load balancer staat het
     * echte adres in de eerste waarde van X-Forwarded-For; anders het
     * directe remote address.
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Operation(summary = "Registreer een gebruiker met workshop- en stationcode")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Gebruiker aangemaakt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer")})
    @PostMapping("register")
    public ResponseEntity<UserResponse> createNewUser(@RequestBody RegisterUserRequest registerRequest) throws MessagingException {
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

        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toUserResponse(user, false));
    }

    @Operation(summary = "Log in met e-mail en wachtwoord (geeft een token of vraagt om een e-mailcode)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer"), @ApiResponse(responseCode = "429", description = "Te veel pogingen")})
    @PostMapping("login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) throws MessagingException {
        throwWhenRateLimited("login", request, loginRequest.email());

        User user = userService.getUserByEmail(loginRequest.email());
        if (user == null || !passwordEncodingService.verifyPassword(loginRequest.password(), user.getPassword())) {
            throw new InvalidArgumentException("Invalid email and/or password");
        }

        // Vertrouwd IP -> mailverificatie overslaan en direct een JWT geven.
        if (trustedIpService.isTrusted(user.getUserId(), clientIp(request))) {
            String token = authService.authenticate(user);
            return ResponseEntity.ok(LoginResponse.authenticated(token));
        }

        // Onbekend IP -> code mailen; de gebruiker doorloopt de verify-stap.
        Token token = tokenService.createVerifyToken(user.getUserId(), TokenType.VERIFY_AUTH);
        emailSenderService.sendLoginMail(user.getEmail(), user.getFirstName(), user.getLastName(), token.getNumericCode());
        return ResponseEntity.ok(LoginResponse.requiresVerification());
    }

    @Operation(summary = "Verifieer de e-mailcode en ontvang een JWT")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer"), @ApiResponse(responseCode = "429", description = "Te veel pogingen")})
    @PostMapping("verify")
    public ResponseEntity<AuthenticationResponse> verifyEmailCode(@RequestBody VerifyLoginRequest verifyLoginRequest, HttpServletRequest request) {
        throwWhenRateLimited("verify", request, verifyLoginRequest.email());

        User user = userService.getUserByEmail(verifyLoginRequest.email());
        if (user == null || !tokenService.verifyToken(verifyLoginRequest.code(), user.getUserId(), TokenType.VERIFY_AUTH)) {
            return ResponseEntity.badRequest().build();
        }

        String token = authService.authenticate(user);
        // Onthoud dit IP zodat de volgende login geen mailcode meer vraagt.
        trustedIpService.remember(user.getUserId(), clientIp(request));
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }

    @Operation(summary = "Stel een nieuw wachtwoord in met een resetcode")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer"), @ApiResponse(responseCode = "429", description = "Te veel pogingen")})
    @PostMapping("reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        throwWhenRateLimited("reset-password", request, resetPasswordRequest.email());

        User user = userService.getUserByEmail(resetPasswordRequest.email());
        if (user == null || !tokenService.verifyToken(resetPasswordRequest.token(), user.getUserId(), TokenType.PASSWORD_RESET)) {
            throw new InvalidArgumentException("Invalid email and/or token");
        }

        user.setPassword(passwordEncodingService.encodePassword(resetPasswordRequest.password()));
        userService.updateUser(user.getUserId(), user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Vraag een wachtwoordresetcode aan per e-mail")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer"), @ApiResponse(responseCode = "429", description = "Te veel pogingen")})
    @PostMapping("forgot-password")
    public ResponseEntity<Void> createForgotPasswordRequest(@RequestBody ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request) throws MessagingException {
        throwWhenRateLimited("forgot-password", request, forgotPasswordRequest.email());

        User user = userService.getUserByEmail(forgotPasswordRequest.email());
        if (user == null) {
            throw new InvalidArgumentException("Invalid email");
        }

        Token token = tokenService.createVerifyToken(user.getUserId(), TokenType.PASSWORD_RESET);
        emailSenderService.sendForgotPasswordMail(user.getEmail(), user.getFirstName(), user.getLastName(), token.getNumericCode());
        return ResponseEntity.ok().build();
    }

    // The authentication endpoints answer errors with a plain-text body (the frontend shows it
    // as-is, see register.jsx). These handlers keep that wire format while the endpoint methods
    // can return their real response types.

    @ExceptionHandler(InvalidArgumentException.class)
    ResponseEntity<String> handleInvalidArgument(InvalidArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    ResponseEntity<String> handleDuplicateEmail(IncorrectResultSizeDataAccessException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is al in gebruik");
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    ResponseEntity<String> handleTooManyAttempts(TooManyAttemptsException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(RATE_LIMIT_MESSAGE);
    }

    @ExceptionHandler({MessagingException.class, MailException.class, DataAccessException.class})
    ResponseEntity<String> handleInfrastructureFailure(Exception e) {
        log.error("Unexpected error in authentication endpoint", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een onverwachte fout opgetreden.");
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
