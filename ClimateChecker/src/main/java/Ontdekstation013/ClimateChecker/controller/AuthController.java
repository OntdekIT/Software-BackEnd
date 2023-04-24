package Ontdekstation013.ClimateChecker.controller;

import Ontdekstation013.ClimateChecker.models.Token;
import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.services.EmailSenderService;
//import Ontdekstation013.ClimateChecker.services.MailService;
import Ontdekstation013.ClimateChecker.services.JWTService;
import Ontdekstation013.ClimateChecker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/Authentication")

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
            Token token = userService.createToken(user);
            userService.saveToken(token);
            emailSenderService.sendSignupMail(user.getMailAddress(), user.getFirstName(), user.getLastName(), userService.createLink(token));
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    // login user
    @PostMapping("login")
    public ResponseEntity<Void> loginUser(@RequestBody loginDto loginDto) throws Exception {
        User user = userService.verifyMail(loginDto);
        if (user != null) {
            Token token = userService.createToken(user);
            userService.saveToken(token);
            emailSenderService.sendLoginMail(user.getMailAddress(), user.getFirstName(), user.getLastName(), userService.createLink(token));
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    // verify the login / (or first register)
    @GetMapping("verify")
    public ResponseEntity<userDto> fetchLink(@RequestParam String linkHash, @RequestParam String email){
        if (userService.verifyToken(linkHash, email)){
            userDto dto = jwtService.generateJWS(userService.getUserByMail(email));
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
}
