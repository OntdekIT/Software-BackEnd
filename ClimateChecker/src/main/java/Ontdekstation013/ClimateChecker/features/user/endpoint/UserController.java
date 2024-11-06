package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.GetAllUsersRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateUserRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
        User user = userService.getUserById(id);
        UserResponse response = UserMapper.toUserResponse(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(GetAllUsersRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.pageSize());
        Page<User> users = userService.getAllUsers(pageable);
        Page<UserResponse> getUserResponse = users.map(UserMapper::toUserResponse);
        return ResponseEntity.ok(getUserResponse);
    public ResponseEntity<?> filterUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean admin) {

        List<UserDto> filteredUsers = userService.getAllUsers(firstName, lastName, email, admin);

        if (filteredUsers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found.");
        } else {
            return ResponseEntity.ok(filteredUsers);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateUserRole(@PathVariable long id, @RequestBody UpdateUserRequest request) {
        User user = userService.getUserById(id);
        user.setAdmin(request.isAdmin());
        userService.updateUser(id, user);
        return ResponseEntity.ok().build();
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

    @PostMapping("grantuseradmin")
    public ResponseEntity<String> grantUserAdmin(@RequestBody GrantUserAdminRequest request) {
        if (request.getUserId() != null & request.getAdminRights() != null) {
            Long userId = Long.parseLong(request.getUserId());
            Boolean adminRights = request.getAdminRights();
            UserDto dto = userService.findUserById(userId);

            if(dto != null)
            {
                dto.setAdmin(adminRights);
                Long returnedUserId = userService.grantUserAdmin(dto);
                return ResponseEntity.status(200).body(returnedUserId.toString());
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User could not be found");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please fill out all fields");
    }
}
