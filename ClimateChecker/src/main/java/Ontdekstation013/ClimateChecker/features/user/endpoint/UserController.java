package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.GetAllUsersRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateUserRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final int MAX_PAGE_SIZE = 100;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable long id, @RequestParam(defaultValue = "false") boolean includeStations) {
        try {
            User user = userService.getUserById(id);
            UserResponse response = UserMapper.toUserResponse(user, includeStations);
            return ResponseEntity.ok(response);
        } catch (InvalidArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een onverwachte fout opgetreden.");
        }
    }

    //TODO: Remove stations from response
    @GetMapping()
    public ResponseEntity<?> getAllUsers(GetAllUsersRequest request) {
        try {
            Pageable pageable = PageRequest.of(request.page(), Math.min(request.pageSize(), MAX_PAGE_SIZE));
            Page<UserResponse> responses = userService.getAllUsers(UserMapper.toUserFilter(request), pageable)
                    .map(user -> UserMapper.toUserResponse(user, false));

            return ResponseEntity.ok(responses);
        } catch (InvalidArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een onverwachte fout opgetreden.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id) {
        try {
            User loggedInUser = (User) userDetails;
            User user = userService.getUserById(id);

            if (loggedInUser.getRole() != UserRole.SUPER_ADMIN && user.getRole() != UserRole.USER) {
                throw new IllegalArgumentException("Only super admins can delete users with special roles");
            }

            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (InvalidArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een onverwachte fout opgetreden.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserRole(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id, @RequestBody UpdateUserRequest request) {
        try {
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
        } catch (InvalidArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een onverwachte fout opgetreden.");
        }
    }
}
