package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final StationRepository stationRepository;

    @Autowired
    public UserService(UserRepository userRepository, StationRepository stationRepository) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
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
        return userRepository.findUsersByOptionalFilters(filter.getFirstName(), filter.getLastName(), filter.getEmail(), filter.getIsAdmin());
    }

    public User getUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return user;
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
            userToUpdate.setAdmin(newUser.isAdmin());
            userRepository.save(userToUpdate);
        } else {
            throw new NotFoundException("User not found");
        }
    }

    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    private Set<Station> getStationsForUser(Long userId) {
        List<Station> stations = stationRepository.findByUserid(userId);
        return new HashSet<>(stations);
    }
}
