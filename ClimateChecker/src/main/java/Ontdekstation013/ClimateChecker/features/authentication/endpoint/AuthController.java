package Ontdekstation013.ClimateChecker.features.authentication.endpoint;

import Ontdekstation013.ClimateChecker.features.authentication.EmailSenderService;
import Ontdekstation013.ClimateChecker.features.authentication.JWTService;
import Ontdekstation013.ClimateChecker.features.authentication.Token;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/api/Authentication")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class AuthController {
    private final UserService userService;
    private final JWTService jwtService;
    private final EmailSenderService emailSenderService;

    @Autowired
    public AuthController(UserService userService, EmailSenderService emailSenderService, JWTService jwtService)
    {
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailSenderService = emailSenderService;
    }

    // create new user
    @PostMapping("register")
    public ResponseEntity<Void> createNewUser(@RequestBody registerDto registerDto) throws Exception {
        User user = userService.createNewUser(registerDto);
        if (user != null){
            Token token = userService.createVerifyToken(user);
            userService.saveToken(token);
            sendVerificationEmail(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    // login user
    @PostMapping("login")
    public ResponseEntity<Void> loginUser(@RequestBody loginDto loginDto) throws Exception {
        User user = userService.login(loginDto);
        if (user != null) {

            Token token = userService.createVerifyToken(user);
            userService.saveToken(token);
            sendVerificationEmail(user);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    private void sendVerificationEmail(User user) throws MessagingException {
        Token token = userService.createVerifyToken(user);
        userService.saveToken(token);
        emailSenderService.sendLoginMail(user.getMailAddress(), user.getFirstName(), user.getLastName(), token.getNumericCode());
    }
    
    @PostMapping("verify")
    public ResponseEntity<Void> verifyEmailCode(@RequestBody verifyDto verifyDto) {
        String email = verifyDto.getMailAddress();
        if (userService.verifyToken(verifyDto.getCode(), email)){
            ResponseCookie cookie = userService.createCookie(new User(userService.getUserByMail(email)));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", cookie.toString() + "; HttpOnly; SameSite=none; Secure");
            return ResponseEntity.status(200).headers(headers).body(null);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
}
