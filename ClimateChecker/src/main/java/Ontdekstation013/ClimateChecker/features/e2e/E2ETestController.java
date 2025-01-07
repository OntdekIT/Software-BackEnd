package Ontdekstation013.ClimateChecker.features.e2e;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.authentication.Token;
import Ontdekstation013.ClimateChecker.features.user.authentication.TokenService;
import Ontdekstation013.ClimateChecker.features.user.authentication.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller with API endpoints to provide support for end-to-end tests.
 * <p>
 * Note: This controller is only available when the e2e profile is active.
 */
@RequiredArgsConstructor
@RestController
@Profile("e2e")
@RequestMapping("/api/e2e")
public class E2ETestController {
    private final UserService userService;
    private final TokenService tokenService;

    /**
     * Resets the data in the database.
     *
     * @return ResponseEntity with status 200 OK
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetData() {
        throw new UnsupportedOperationException("Not implemented yet");
//        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves an authentication token for a user by their email address.
     *
     * @param email the email address of the user
     * @return ResponseEntity with status 200 OK
     */
    @GetMapping("/users/{email}/auth-token")
    public ResponseEntity<?> getAuthToken(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        Token token = tokenService.getTokenByUserIdAndTokenType(user.getUserId(), TokenType.VERIFY_AUTH);

        if (token == null) {
            token = tokenService.createVerifyToken(user.getUserId(), TokenType.VERIFY_AUTH);
        }

        return ResponseEntity.ok(token);
    }
}