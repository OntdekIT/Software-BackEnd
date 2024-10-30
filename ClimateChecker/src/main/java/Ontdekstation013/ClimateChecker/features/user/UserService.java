package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.station.IStationRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.RegisterRequest;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static Ontdekstation013.ClimateChecker.features.user.authentication.PasswordUtils.HashPassword;

@Service
public class UserService {

    private final IUserRepository userRepository;
    private final IStationRepository stationRepository;
    private final WorkshopCodeService workshopCodeService;

    @Autowired
    public UserService(IUserRepository userRepository, IStationRepository stationRepository, WorkshopCodeService workshopCodeService) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.workshopCodeService = workshopCodeService;
    }

    public User createNewUser(RegisterRequest registerRequest) throws Exception {
        UserValidator userValidator = new UserValidator();
        ValidationResult result = userValidator.validate(registerRequest, userRepository, stationRepository, workshopCodeService);
        if (!result.isValid()) {
            throw new Exception(result.getErrorMessage());
        }
        User user = new User(registerRequest.email(), registerRequest.firstName(), registerRequest.lastName(), registerRequest.password());
        user.setPassword(HashPassword(user.getPassword()));

        userRepository.save(user);
        Station station = stationRepository.getByRegistrationCode(registerRequest.stationCode());
        station.setUserid(user.getUserId());

        return user;
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
}
