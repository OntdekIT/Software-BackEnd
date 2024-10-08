package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import Ontdekstation013.ClimateChecker.features.user.authentication.JWTService;
import Ontdekstation013.ClimateChecker.features.user.authentication.Token;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<String> createNewUser(@RequestBody RegisterDto registerDto) throws Exception {
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
        }
    }

    @PostMapping("login")
    public ResponseEntity<String> loginUser(@RequestBody LoginDto loginDto) throws Exception {
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
    public ResponseEntity<Void> verifyEmailCode(@RequestBody VerifyDto verifyDto) {
        String email = verifyDto.getMailAddress();
        if (userService.verifyToken(verifyDto.getCode(), email)){
            ResponseCookie cookie = userService.createCookie(new User(userService.getUserByMail(email)));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie", cookie.toString() + "; HttpOnly; SameSite=none; Secure");
            return ResponseEntity.status(200).headers(headers).body(null);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @GetMapping("checkLogin")
    public ResponseEntity<Boolean> verifyLogin(HttpServletResponse response, HttpServletRequest request){
        if (request.getCookies() != null) {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
    }

    @DeleteMapping("logout")
    public ResponseEntity<String> clearCookies(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        return ResponseEntity.ok().body(null);
    }
}