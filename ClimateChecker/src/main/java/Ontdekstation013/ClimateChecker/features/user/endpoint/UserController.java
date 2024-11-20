package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.GetAllUsersRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateUserRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id, @RequestParam(defaultValue = "false") boolean includeStations) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Gebruikersnaam: " + auth.getName());
        System.out.println("Authorities: " + auth.getAuthorities());
        User user = userService.getUserById(id);
        UserResponse response = UserMapper.toUserResponse(user, includeStations);
        return ResponseEntity.ok(response);
    }

    //TODO: Re-add pagination
    //TODO: Remove stations from response
    @GetMapping("all")
    public ResponseEntity<List<UserResponse>> getAllUsers(GetAllUsersRequest request) {
        List<User> users = userService.getAllUsers(UserMapper.toUserFilter(request));
        List<UserResponse> responses = new ArrayList<>();

        for (User user : users) {
            responses.add(UserMapper.toUserResponse(user, false));
        }

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserRole(@PathVariable long id, @RequestBody UpdateUserRequest request) {
        User user = userService.getUserById(id);
        user.setAdmin(request.isAdmin());
        userService.updateUser(id, user);
        return ResponseEntity.ok().build();
    }
}
