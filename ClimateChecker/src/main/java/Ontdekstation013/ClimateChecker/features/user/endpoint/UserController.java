package Ontdekstation013.ClimateChecker.features.user.endpoint;


import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/User")
public class UserController {
    private final UserService userService;
    private final EmailSenderService emailSenderService;

    @Autowired
    public UserController(UserService userService, EmailSenderService emailSEnderService) {
        this.userService = userService;
        this.emailSenderService = emailSEnderService;
    }

    @GetMapping("{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long userId) {
        UserDto dto = userService.findUserById(userId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(){
        throw new UnsupportedOperationException();
    }

    @GetMapping("page/{pageNumber}")
    public ResponseEntity<List<UserDto>> getAllUsersByPage(@PathVariable long pageId) {
        throw new UnsupportedOperationException();
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable long userId){
        throw new UnsupportedOperationException();
    }

    @PutMapping("/role")
    public ResponseEntity<String> grantUserAdmin(@RequestBody GrantUserAdminRequest request) {
        throw new UnsupportedOperationException();
    }
}
