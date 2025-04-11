package Ontdekstation013.ClimateChecker.user.authentication;

import Ontdekstation013.ClimateChecker.config.JwtService;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.authentication.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationUnitTests {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        User testUser = new User(
                1L,
                "Test",
                "User",
                "test@example.com",
                UserRole.USER,
                "securepassword123"
        );
    }

    @Test
    public void testAuthenticate_ReturnsToken() {
        // Arrange
        String expectedToken = "mocked-token-123";
        when(jwtService.generateToken(testUser)).thenReturn(expectedToken);

        // Act
        String result = authenticationService.authenticate(testUser);

        // Assert
        assertEquals(expectedToken, result);
        verify(jwtService, times(1)).generateToken(testUser);
    }
}
