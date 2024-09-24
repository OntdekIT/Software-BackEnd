package Ontdekstation013.ClimateChecker.features.user.endpoint;


import Ontdekstation013.ClimateChecker.features.authentication.EmailSenderService;
import Ontdekstation013.ClimateChecker.features.user.UserConverter;
import Ontdekstation013.ClimateChecker.features.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/api/User")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
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
    public ResponseEntity<UserDto> getUserById(@PathVariable long userId){
        UserDto dto = userService.findUserById(userId);
        return ResponseEntity.ok(dto);
    }

    // get all users
    @GetMapping
    public ResponseEntity<List<UserDataDto>> getAllUsers(){
        List<UserDataDto> newDtoList = userService.getAllUsers();
        return ResponseEntity.ok(newDtoList);
    }

    // get users by page number
    @GetMapping("page/{pageNumber}")
    public ResponseEntity<List<UserDataDto>> getAllUsersByPage(@PathVariable long pageId){
        List<UserDataDto> newDtoList = userService.getAllByPageId(pageId);
        return ResponseEntity.ok(newDtoList);
    }

    // edit user
//    @PutMapping
//    public ResponseEntity<userDto> editUser(@RequestBody editUserDto editUserDto) throws Exception {
//        User user = userService.editUser(editUserDto);
//        if (user != null) {
//            Token token = null;//userService.createCookie(user);
//            token.setUserid(user.getUserID());
//            userService.saveToken(token);
//            emailSenderService.sendEmailEditMail(editUserDto.getMailAddress(), user.getFirstName(), user.getLastName(), userService.createLink(token, editUserDto.getMailAddress()));
//        }
//
//        return ResponseEntity.status(HttpStatus.OK).body(userConverter.userToUserDto(user));
//    }

    // delete user
    @DeleteMapping("{userId}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable long userId){
        UserDto user = userService.deleteUser(userId);
        emailSenderService.deleteUserMail(user.getMailAddress(), user.getFirstName(), user.getLastName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("getName")
    public ResponseEntity<String> getName(HttpServletResponse response, HttpServletRequest request){
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            Long userID = Long.parseLong(cookies[0].getValue());
            UserDto user = userService.findUserById(userID);
        return ResponseEntity.ok(user.getFirstName());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("getUser")
    public ResponseEntity<UserDto> getUser(HttpServletResponse response, HttpServletRequest request){
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            Long userID = Long.parseLong(cookies[0].getValue());
            UserDto user = userService.findUserById(userID);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("getID")
    public ResponseEntity<String> getId(HttpServletResponse response, HttpServletRequest request){
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            Long userID = Long.parseLong(cookies[0].getValue());
            return ResponseEntity.status(HttpStatus.OK).body(userID.toString());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("checkAdmin")
    public ResponseEntity<Boolean> checkAdmin(HttpServletResponse response, HttpServletRequest request){
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            Long userID = Long.parseLong(cookies[0].getValue());
            UserDto user = userService.findUserById(userID);
            return ResponseEntity.status(HttpStatus.OK).body(user.getAdmin());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
