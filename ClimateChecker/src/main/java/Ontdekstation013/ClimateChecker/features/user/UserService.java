package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final IUserRepository userRepository;

    @Autowired
    public UserService(IUserRepository userRepository) {
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

    public List<UserDto> getAllUsers(String firstName, String lastName, String mailAddress, Boolean admin) {
        List<User> userList = userRepository.findUsersByOptionalFilters(
                firstName != null && !firstName.isEmpty() ? firstName : null,
                lastName != null && !lastName.isEmpty() ? lastName : null,
                mailAddress != null && !mailAddress.isEmpty() ? mailAddress : null,
                admin
        );

        return userList.stream()
                .map(user -> userConverter.userToUserDto(user))
                .collect(Collectors.toList());
    }

    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
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

//    public User getUserWithSations(Long userid) {
//            User user = userRepository.findById(userid)
//               .orElseThrow(() -> new NotFoundException("User not found"));
//
//    }
}
