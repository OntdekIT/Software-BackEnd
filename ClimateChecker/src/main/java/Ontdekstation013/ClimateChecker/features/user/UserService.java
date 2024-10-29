package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.station.IStationRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.RegisterRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateUserRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        throw new UnsupportedOperationException();
    }

    public List<User> getAllByPageId(long pageId) {
        throw new UnsupportedOperationException();
    }

    public Optional<User> updateUser(long id, UpdateUserRequest updatedUser) throws Exception {
        return userRepository.findById(id).map(
                user -> {
                    user.setFirstName(user.getFirstName());
                    user.setLastName(user.getLastName());
                    user.setEmail(user.getEmail());
                    user.setPassword(user.getPassword());
                    return userRepository.save(user);
                });
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Long grantUserAdmin(UserResponse dto) {
        User updatedUser = new User(dto);
        return userRepository.save(updatedUser).getUserId();
    }
}
