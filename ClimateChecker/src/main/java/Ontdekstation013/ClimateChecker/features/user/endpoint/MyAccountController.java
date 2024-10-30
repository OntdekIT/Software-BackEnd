package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateMyAccountRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.utility.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/my-account")
public class MyAccountController {
    private final UserService userService;

    public MyAccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/name")
    public ResponseEntity<String> getName(HttpServletResponse response, HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @GetMapping()
    public ResponseEntity<UserResponse> getUser(HttpServletResponse response, HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }

    @GetMapping("/id")
    public ResponseEntity<?> getId(HttpServletResponse response, HttpServletRequest request) {
        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = AuthHelper.getNullableUserIdFromRequestCookie(request);

        if (userId != null) {
            responseEntity = ResponseEntity.status(HttpStatus.OK).body(userId.toString());
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

    @GetMapping("/admin")
    public ResponseEntity<Boolean> checkRole(HttpServletResponse response, HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }
}
