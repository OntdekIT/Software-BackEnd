package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.config.JwtService;
import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.AuthenticationRequest;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.AuthenticationResponse;
import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse createNewUser(User user) {
        UserValidator validator = new UserValidator();
        ValidationResult result = validator.validate(user);

        if (userRepository.existsUserByEmail(user.getEmail())) {
            throw new InvalidArgumentException("Email already exists");
        }

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new NotFoundException("User not found");
        }


        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();

    }


    public List<User> getAllUsers(UserFilter filter) {
        return userRepository.findUsersByOptionalFilters(filter.getFirstName(), filter.getLastName(), filter.getEmail(), filter.getRole());
    }

    public List<User> getUsersByWorkshop(Workshop workshop) {
        return userRepository.findByWorkshop(workshop);
    }

    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void updateUser(long id, User newUser) {
        Optional<User> getUserResult = userRepository.findById(id);

        if (getUserResult.isPresent()) {
            User userToUpdate = getUserResult.get();
            userToUpdate.setFirstName(newUser.getFirstName());
            userToUpdate.setLastName(newUser.getLastName());
            userToUpdate.setEmail(newUser.getEmail());
            userToUpdate.setPassword(newUser.getPassword());
            userToUpdate.setRole(newUser.getRole());
            userRepository.save(userToUpdate);
        } else {
            throw new NotFoundException("User not found");
        }
    }

    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}
