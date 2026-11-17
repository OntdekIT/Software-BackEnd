package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.user.PasswordEncodingService;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserMapper;
import Ontdekstation013.ClimateChecker.features.user.UserService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateMyAccountRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "Mijn account", description = "Gegevens van de ingelogde gebruiker")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/my-account")
public class MyAccountController {
    private final UserService userService;
    private final PasswordEncodingService passwordEncodingService;

    @Operation(summary = "Geef het account van de ingelogde gebruiker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "401", description = "Niet ingelogd")})
    @GetMapping()
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "false") boolean includeStations) {
        User user = userService.getUserById(Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(UserMapper.toUserResponse(user, includeStations));
    }

    @Operation(summary = "Wijzig naam, e-mail of wachtwoord van de ingelogde gebruiker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Gelukt"), @ApiResponse(responseCode = "400", description = "Ongeldige invoer"), @ApiResponse(responseCode = "401", description = "Niet ingelogd")})
    @PutMapping()
    public ResponseEntity<Void> updateUser(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody UpdateMyAccountRequest updateRequest) {
        User user = userService.getUserById(Long.parseLong(userDetails.getUsername()));
        user.setEmail(updateRequest.email());
        user.setFirstName(updateRequest.firstName());
        user.setLastName(updateRequest.lastName());

        if (updateRequest.password() != null && !updateRequest.password().isEmpty()) {
            user.setPassword(passwordEncodingService.encodePassword(updateRequest.password()));
        }

        userService.updateUser(user.getUserId(), user);
        return ResponseEntity.ok().build();
    }
}
