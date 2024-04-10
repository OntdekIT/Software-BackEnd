package Ontdekstation013.ClimateChecker.features.user.endpoint;


import Ontdekstation013.ClimateChecker.features.authentication.EmailSenderService;
import Ontdekstation013.ClimateChecker.features.authentication.Token;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserConverter;
import Ontdekstation013.ClimateChecker.features.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/User")
public class UserController {

    private final UserService userService;
    private final UserConverter userConverter;

    private final EmailSenderService emailSenderService;
    @Autowired
    public UserController(UserService userService, UserConverter userConverter , EmailSenderService emailSEnderService){
        this.userService = userService;
        this.userConverter = userConverter;
        this.emailSenderService = emailSEnderService;
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
            Token token = null;//userService.createCookie(user);
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
