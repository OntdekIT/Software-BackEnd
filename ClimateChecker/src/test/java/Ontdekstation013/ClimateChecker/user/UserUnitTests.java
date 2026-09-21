package Ontdekstation013.ClimateChecker.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import Ontdekstation013.ClimateChecker.config.JwtService;
import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.user.*;
import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class UserUnitTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private Workshop workshop;
    private User user;
    private User user2;

    @BeforeEach
    public void setUp() {
        user = new User(1L, "John", "Doe", "john.doe@email.com", UserRole.USER, "MyPassword123");
        user2 = new User(2L, "Jane", "Doe", "jane.doe@email.com", UserRole.USER, "MyPassword123");
    }


    @Test
    public void createUser_ValidatesFields_Succeeds() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createNewUser(user);

        assertNotNull(createdUser);
        assertEquals(user.getEmail(), createdUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void createNewUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsUserByEmail(user.getEmail())).thenReturn(true);

        assertThrows(InvalidArgumentException.class, () -> userService.createNewUser(user));
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    public void getUsersWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        List<User> users = Arrays.asList(user, new User(2L, "Jane", "Doe", "jane.doe@email.com", UserRole.USER, "AnotherPassword123"));
        Page<User> page = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findUsersByOptionalFilters(null, null, null, null, pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(new UserFilter(), pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(userRepository, times(1)).findUsersByOptionalFilters(null, null, null, null, pageable);
    }

    @Test
    public void getUsersWithOptionalFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        List<User> users = Collections.singletonList(user);
        Page<User> page = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findUsersByOptionalFilters("John", "Doe", "john.doe@email.com", UserRole.USER, pageable)).thenReturn(page);

        UserFilter filter = new UserFilter("John", "Doe", "john.doe@email.com", UserRole.USER);
        Page<User> result = userService.getAllUsers(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findUsersByOptionalFilters("John", "Doe", "john.doe@email.com", UserRole.USER, pageable);
    }

    @Test
    public void getUserById_Succeeds() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void getUserById_WhenUserIsNotFound_FailsAndThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void getUserByEmail_Succeeds() {
        when(userRepository.findByEmail("john.doe@email.com")).thenReturn(user);

        User result = userService.getUserByEmail("john.doe@email.com");

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail("john.doe@email.com");
    }

    @Test
    public void updateUser_WhenUserFound_Succeeds() {
        User updatedUser = new User(1L, "John", "Smith", "john.smith@email.com", UserRole.USER, "NewPassword123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        userService.updateUser(1L, updatedUser);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateUser_WhenUserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        User updatedUser = new User(1L, "John", "Smith", "john.smith@email.com", UserRole.USER, "NewPassword123");

        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, updatedUser));
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void deleteUser_Succeeds() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void getUsersByWorkshopCode_ReturnsUsers() {
        when(userRepository.findByWorkshop(workshop)).thenReturn(Arrays.asList(user, user2));

        List<User> users = userService.getUsersByWorkshop(workshop);

        assertEquals(2, users.size());
        assertEquals(user.getEmail(), users.get(0).getEmail());
        assertEquals(user2.getEmail(), users.get(1).getEmail());
        verify(userRepository, times(1)).findByWorkshop(workshop);
    }
}
