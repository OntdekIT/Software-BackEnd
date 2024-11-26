package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateMyAccountRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.utility.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/my-account")
public class MyAccountController {
    private final UserService userService;

    public MyAccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<UserResponse> getUser(HttpServletRequest request, @RequestParam(defaultValue = "false") boolean includeStations) {
        ResponseEntity<UserResponse> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = AuthHelper.getNullableUserIdFromRequestCookie(request);

        if (userId != null) {
            User user = userService.getUserById(userId);
            UserResponse userResponse = UserMapper.toUserResponse(user, includeStations);
            responseEntity = ResponseEntity.ok(userResponse);
        }

        return responseEntity;
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(HttpServletRequest request, UpdateMyAccountRequest updateMyAccountRequest) {
        long userId = AuthHelper.getUserIdFromRequestCookie(request);
        User user = userService.getUserById(userId);
        user = UserMapper.toUpdatedUser(user, updateMyAccountRequest);
        userService.updateUser(userId, user);
        return ResponseEntity.ok().build();
    }
}
