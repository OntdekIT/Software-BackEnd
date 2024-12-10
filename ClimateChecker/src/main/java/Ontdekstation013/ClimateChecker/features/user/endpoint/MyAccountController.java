package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateMyAccountRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-account")
public class MyAccountController {
    private final UserService userService;

    public MyAccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "false") boolean includeStations) {
        User user = userService.getUserById(Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(UserMapper.toUserResponse(user, includeStations));
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UpdateMyAccountRequest updateMyAccountRequest) {
        User user = userService.getUserById(Long.parseLong(userDetails.getUsername()));
        user = UserMapper.toUpdatedUser(user, updateMyAccountRequest);
        userService.updateUser(user.getUserId(), user);
        return ResponseEntity.ok().build();
    }
}
