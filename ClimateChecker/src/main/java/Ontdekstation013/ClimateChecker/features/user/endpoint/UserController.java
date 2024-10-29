package Ontdekstation013.ClimateChecker.features.user.endpoint;


import Ontdekstation013.ClimateChecker.features.user.UserConverter;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
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
    public UserController(UserService userService, UserConverter userConverter, EmailSenderService emailSEnderService) {
        this.userService = userService;
        this.userConverter = userConverter;
        this.emailSenderService = emailSEnderService;
    }

    // get user by id
    @GetMapping("{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long userId) {
        UserDto dto = userService.findUserById(userId);
        return ResponseEntity.ok(dto);
    }

    // get all users
    @GetMapping("/filter")
    public ResponseEntity<List<UserDto>> filterUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String mailAddress) {

        List<UserDto> filteredUsers = userService.getAllUsers(firstName, lastName, mailAddress);
        return ResponseEntity.ok(filteredUsers);
    }

    // get users by page number
    @GetMapping("page/{pageNumber}")
    public ResponseEntity<List<UserDataDto>> getAllUsersByPage(@PathVariable long pageId) {
        List<UserDataDto> newDtoList = userService.getAllByPageId(pageId);
        return ResponseEntity.ok(newDtoList);
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable long userId) {
        UserDto user = userService.deleteUser(userId);
        emailSenderService.deleteUserMail(user.getMailAddress(), user.getFirstName(), user.getLastName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("getName")
    public ResponseEntity<String> getName(HttpServletResponse response, HttpServletRequest request) {
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
    public ResponseEntity<UserDto> getUser(HttpServletResponse response, HttpServletRequest request) {
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
    public ResponseEntity<String> getId(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            Long userID = Long.parseLong(cookies[0].getValue());
            return ResponseEntity.status(HttpStatus.OK).body(userID.toString());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("checkAdmin")
    public ResponseEntity<Boolean> checkAdmin(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies;
        if (request.getCookies() != null) {
            cookies = request.getCookies();
            Long userID = Long.parseLong(cookies[0].getValue());
            UserDto user = userService.findUserById(userID);
            return ResponseEntity.status(HttpStatus.OK).body(user.getAdmin());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("grantuseradmin")
    public ResponseEntity<String> grantUserAdmin(@RequestBody GrantUserAdminRequest request) {
        if (request.getUserId() != null & request.getAdminRights() != null) {
            Long userId = Long.parseLong(request.getUserId());
            Boolean adminRights = request.getAdminRights();
            UserDto dto = userService.findUserById(userId);

            if (dto != null) {
                dto.setAdmin(adminRights);
                Long returnedUserId = userService.grantUserAdmin(dto);
                return ResponseEntity.status(200).body(returnedUserId.toString());
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User could not be found");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please fill out all fields");
    }

}
