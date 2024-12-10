package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.GetAllUsersRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateUserRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    @GetMapping()
    public ResponseEntity<List<UserResponse>> getAllUsers(GetAllUsersRequest request) {
        List<User> users = userService.getAllUsers(UserMapper.toUserFilter(request));
        List<UserResponse> responses = new ArrayList<>();

        for (User user : users) {
            responses.add(UserMapper.toUserResponse(user, false));
        }

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id) {
        User loggedInUser = (User) userDetails;
        User user = userService.getUserById(id);

        if (loggedInUser.getRole() != UserRole.SUPER_ADMIN && user.getRole() != UserRole.USER) {
            throw new IllegalArgumentException("Only super admins can delete users with special roles");
        }

        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserRole(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id, @RequestBody UpdateUserRequest request) {
        User loggedInUser = (User) userDetails;

        if (request.role() == UserRole.SUPER_ADMIN && loggedInUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Only super admins can assign super admin roles");
        }

        User user = userService.getUserById(id);

        if (user.getRole() == UserRole.SUPER_ADMIN && loggedInUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Only super admins can update super admin roles");
        } else if (user.getRole() != UserRole.USER && request.role() == UserRole.USER && loggedInUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Only super admins can downgrade roles");
        }

        user.setRole(request.role());
        userService.updateUser(id, user);
        return ResponseEntity.ok().build();
    }
}
