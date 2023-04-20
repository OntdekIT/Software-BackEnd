package Ontdekstation013.ClimateChecker.controller;


import Ontdekstation013.ClimateChecker.models.Token;
import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.services.EmailSenderService;
import Ontdekstation013.ClimateChecker.Services.StationService;
import Ontdekstation013.ClimateChecker.Services.UserService;
import Ontdekstation013.ClimateChecker.Services.converters.UserConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import Ontdekstation013.ClimateChecker.models.dto.*;


@RestController
@RequestMapping("/api/User")
public class UserController {

    private final UserService userService;
    private UserConverter userConverter;

    private final EmailSenderService emailSenderService;
    @Autowired
    public UserController(UserService userService, EmailSenderService emailSEnderService){
        this.userService = userService;
        this.emailSenderService = emailSEnderService;
        this.userConverter = new UserConverter();
    }

    // get user by id
    @GetMapping("{userId}")
    public ResponseEntity<userDto> getUserById(@PathVariable long userId){
        userDto dto = userService.findUserById(userId);
        return ResponseEntity.ok(dto);
    }

    // get all users
    @GetMapping
    public ResponseEntity<List<userDataDto>> getAllUsers(){
        List<userDataDto> newDtoList = userService.getAllUsers();
        return ResponseEntity.ok(newDtoList);
    }

    // get users by page number
    @GetMapping("page/{pageNumber}")
    public ResponseEntity<List<userDataDto>> getAllUsersByPage(@PathVariable long pageId){
        List<userDataDto> newDtoList = userService.getAllByPageId(pageId);
        return ResponseEntity.ok(newDtoList);
    }

    // edit user
    @PutMapping
    public ResponseEntity<userDto> editUser(@RequestBody editUserDto editUserDto) throws Exception {
        User user = userService.editUser(editUserDto);
        if (user != null) {
            Token token = userService.createToken(user);
            token.setUser(user);
            userService.saveToken(token);
            emailSenderService.sendEmailEditMail(editUserDto.getMailAddress(), user.getFirstName(), user.getLastName(), userService.createLink(token, editUserDto.getMailAddress()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(userConverter.userToUserDto(user));
    }

    // delete user
    @DeleteMapping("{userId}")
    public ResponseEntity<userDto> deleteUser(@PathVariable long userId){
        userDto user = userService.deleteUser(userId);
        emailSenderService.deleteUserMail(user.getMailAddress(), user.getFirstName(), user.getLastName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("verify")
    public ResponseEntity<userDto> fetchLink(@RequestParam String linkHash, @RequestParam String oldEmail, @RequestParam String newEmail){
        if (userService.verifyToken(linkHash, oldEmail, newEmail)){
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
}
