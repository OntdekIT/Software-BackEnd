package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createNewUser(User user) {
        UserValidator validator = new UserValidator();
        ValidationResult result = validator.validate(user);

        if (userRepository.existsUserByEmail(user.getEmail())) {
            throw new InvalidArgumentException("Email already exists");
        }

        return userRepository.save(user);
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
