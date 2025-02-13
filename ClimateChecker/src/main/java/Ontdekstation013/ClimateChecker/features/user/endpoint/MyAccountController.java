package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.user.PasswordEncodingService;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateMyAccountRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/my-account")
public class MyAccountController {
    private final UserService userService;
    private final PasswordEncodingService passwordEncodingService;

    @GetMapping()
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "false") boolean includeStations) {
        try {
            User user = userService.getUserById(Long.parseLong(userDetails.getUsername()));
            return ResponseEntity.ok(UserMapper.toUserResponse(user, includeStations));
        }  catch (InvalidArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een onverwachte fout opgetreden.");
        }
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody UpdateMyAccountRequest updateRequest) {
        try {
            User user = userService.getUserById(Long.parseLong(userDetails.getUsername()));
            user.setEmail(updateRequest.email());
            user.setFirstName(updateRequest.firstName());
            user.setLastName(updateRequest.lastName());

            if (updateRequest.password() != null && !updateRequest.password().isEmpty()) {
                user.setPassword(passwordEncodingService.encodePassword(updateRequest.password()));
            }

            userService.updateUser(user.getUserId(), user);
            return ResponseEntity.ok().build();
        }  catch (InvalidArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Er is een onverwachte fout opgetreden.");
        }
    }
}
