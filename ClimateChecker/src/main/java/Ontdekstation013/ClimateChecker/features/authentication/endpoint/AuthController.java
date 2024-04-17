package Ontdekstation013.ClimateChecker.features.authentication.endpoint;

import Ontdekstation013.ClimateChecker.exception.OntdekstationException;
import Ontdekstation013.ClimateChecker.features.authentication.EmailSenderService;
import Ontdekstation013.ClimateChecker.features.authentication.JWTService;
import Ontdekstation013.ClimateChecker.features.authentication.Token;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public ResponseEntity<String> createNewUser(@RequestBody registerDto registerDto) throws Exception {
        try {
            User user = userService.createNewUser(registerDto);

            if (user != null){

                Token token = userService.createVerifyToken(user);
                emailSenderService.sendLoginMail(user.getMailAddress(), user.getFirstName(), user.getLastName(), token.getNumericCode());
                return ResponseEntity.status(HttpStatus.CREATED).body(null);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        catch (Exception ex)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
            //OntdekstationException exception = new OntdekstationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
            //return new ResponseEntity<OntdekstationException>(exception, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // login user
    @PostMapping("login")
    public ResponseEntity<String> loginUser(@RequestBody loginDto loginDto) throws Exception {
        try{
            User user = userService.login(loginDto);
            if (user != null) {
                Token token = userService.createVerifyToken(user);
                emailSenderService.sendLoginMail(user.getMailAddress(), user.getFirstName(), user.getLastName(), token.getNumericCode());
                return ResponseEntity.status(HttpStatus.OK).body(null);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
       catch (Exception error){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error.getMessage());
       }
    }

    @PostMapping("verify")
    public ResponseEntity<Void> verifyLink(@RequestBody verifyDto verifyDto, HttpServletResponse response, HttpServletRequest request) {
        if (userService.verifyToken(verifyDto.getCode(), verifyDto.getMailAddress())){
            Cookie cookie = userService.createCookie(new User(userService.getUserByMail(verifyDto.getMailAddress())));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", "token=" + cookie.toString() + "; HttpOnly; SameSite=none; Secure");
            return ResponseEntity.status(200).headers(headers).body(null);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
}
