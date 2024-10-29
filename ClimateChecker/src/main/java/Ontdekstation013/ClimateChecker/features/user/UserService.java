package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.station.IStationRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.RegisterDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.EditUserDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.UserDto;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public User createNewUser(RegisterDto registerDto) throws Exception {
        UserValidator userValidator = new UserValidator();
        ValidationResult result = userValidator.validate(registerDto, userRepository, stationRepository, workshopCodeService);
        if (!result.isValid()) {
            throw new Exception(result.getErrorMessage());
        }
        User user = new User(registerDto.email(), registerDto.firstName(), registerDto.lastName(), registerDto.password());
        user.setPassword(HashPassword(user.getPassword()));

        userRepository.save(user);
        Station station = stationRepository.getByRegistrationCode(registerDto.stationCode());
        station.setUserid(user.getUserId());

        return user;
    }

    public UserDto findUserById(long id) {
        throw new UnsupportedOperationException();
    }

    public List<UserDto> getAllUsers() {
        throw new UnsupportedOperationException();
    }

    public List<UserDto> getAllByPageId(long pageId) {
        throw new UnsupportedOperationException();
    }

    public User editUser(EditUserDto editUserDto) throws Exception {
        throw new UnsupportedOperationException();
    }

    public void deleteUser(Long id) {
        throw new UnsupportedOperationException();
    }

    public UserDto getUserByEmailMail(String email) {
        throw new UnsupportedOperationException();
    }

    public Long grantUserAdmin(UserDto dto) {
        User updatedUser = new User(dto);
        return userRepository.save(updatedUser).getUserId();
    }
}
