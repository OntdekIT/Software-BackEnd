package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.exception.ExistingUniqueIdentifierException;
import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.station.IStationRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.user.authentication.ITokenRepository;
import Ontdekstation013.ClimateChecker.features.user.authentication.JWTService;
import Ontdekstation013.ClimateChecker.features.user.authentication.Token;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.LoginDto;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.RegisterDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.EditUserDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.UserDataDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.UserDto;
import Ontdekstation013.ClimateChecker.features.workshopCode.WorkshopCodeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final IUserRepository userRepository;
    private final IStationRepository stationRepository;
    private final ITokenRepository ITokenRepository;
    private final WorkshopCodeService adminService;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JWTService jwtService;


    private final UserConverter userConverter;

    @Autowired
    public UserService(IUserRepository userRepository, IStationRepository stationRepository, ITokenRepository ITokenRepository, WorkshopCodeService adminService, JWTService jwtService) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.ITokenRepository = ITokenRepository;
        this.adminService = adminService;
        this.userConverter = new UserConverter();
        this.jwtService = jwtService;
    }

    public UserDto findUserById(long id) {
        User user = userRepository.findById(id).get();
        return user.toDto();
    }


    // not yet functional
    public List<UserDto> getAllUsers(String firstName, String lastName, String mailAddress) {
        List<User> userList = userRepository.findUsersByOptionalFilters(
                firstName != null && !firstName.isEmpty() ? firstName : null,
                lastName != null && !lastName.isEmpty() ? lastName : null,
                mailAddress != null && !mailAddress.isEmpty() ? mailAddress : null
        );

        return userList.stream()
                .map(user -> userConverter.userToUserDto(user))
                .collect(Collectors.toList());
    }

    // not yet functional
    // why?
    public List<UserDataDto> getAllByPageId(long pageId) {
        List<UserDataDto> newDtoList = new ArrayList<UserDataDto>();

        return newDtoList;
    }

    public User editUser(EditUserDto editUserDto) throws Exception {
        User user = userRepository.findById(editUserDto.getId()).orElseThrow();

        if (editUserDto.getFirstName().length() < 256)
            user.setFirstName(editUserDto.getFirstName());
        else
            throw new InvalidArgumentException("First name can't be longer than 256 characters");

        if (editUserDto.getLastName().length() < 256)
            user.setLastName(editUserDto.getLastName());
        else
            throw new InvalidArgumentException("Last name can't be longer than 256 characters");

        //TODO Wachtwoord veranderen toevoegen
        editUserDto.setMailAddress(editUserDto.getMailAddress().toLowerCase());
        if (!user.getMailAddress().equals(editUserDto.getMailAddress())) {
            if (editUserDto.getMailAddress().contains("@")) {
                if (!userRepository.existsUserByMailAddress(editUserDto.getMailAddress())) {
                    String mail = user.getMailAddress();
                    user.setMailAddress(editUserDto.getMailAddress());
                    userRepository.save(user);
                    userRepository.save(user);
                    user.setMailAddress(mail);
                } else {
                    throw new ExistingUniqueIdentifierException("Email already in use");
                }
            } else {
                throw new InvalidArgumentException("Invalid email address");
            }
        }

        return user;
    }

    public UserDto deleteUser(Long id) {
        User user = userRepository.getById(id);
        userRepository.deleteById(id);
        return (userConverter.userToUserDto(user));
    }

    public User createNewUser(RegisterDto registerDto) throws Exception {
        try {
            User user = new User();
            if (user.ValidateInput(registerDto)) {
                registerDto.setMailAddress(registerDto.getMailAddress().toLowerCase());
                if (!userRepository.existsUserByMailAddress(registerDto.getMailAddress())) { //check if email is unique
                    user = new User(registerDto.getMailAddress(), registerDto.getFirstName(), registerDto.getLastName(), registerDto.getPassword());
                } else {
                    throw new ExistingUniqueIdentifierException("Email already in use");
                }
            } else {
                throw new InvalidArgumentException("Invalid information");
            }

            user.setPassword(HashPassword(user.getPassword()));

            Station station = stationRepository.getByRegistrationCode(registerDto.getMeetstationCode().longValue());
            if (station != null) {
                if (station.getUserid() == null) {
                    if (adminService.VerifyWorkshopCode(registerDto.getWorkshopCode().longValue())) {
                        user = userRepository.save(user);
                        station.setUserid(user.getUserID());
                        return user;
                    }

                    throw new Exception("Workshopcode is niet geldig");
                }

                throw new Exception("Meetstation is al in gebruik");
            }

            throw new Exception("Meetstation bestaat niet");


        } catch (Exception ex) {
            throw ex;
        }

    }

    public User login(LoginDto loginDto) throws Exception {
        User user = userRepository.findByMailAddress(loginDto.getMailAddress());
        if (verifyPassword(loginDto.getPassword(), user.getPassword())) {
            return userRepository.findByMailAddress(loginDto.getMailAddress());
        }
        throw new NotFoundException("User not found");
    }

    public UserDto getUserByMail(String mail) {
        ModelMapper mapper = new ModelMapper();
        UserDto dto = new UserDto();
        UserConverter converter = new UserConverter();
        dto = converter.userToUserDto(userRepository.findByMailAddress(mail));
        //dto = new userDto(mail);
        return dto;
    }


    public ResponseCookie createCookie(User user) {
        Cookie jwtTokenCookie = new Cookie("user-id", user.getUserID().toString());
        ResponseCookie springCookie = ResponseCookie.from(jwtTokenCookie.getName(), jwtTokenCookie.getValue())
                .httpOnly(true)
                .sameSite("None")
                .path("/")
                .build();

        return springCookie;
    }

    public Long grantUserAdmin(UserDto dto) {
        User updatedUser = new User(dto);
        return userRepository.save(updatedUser).getUserID();
    }

    public Token createVerifyToken(User user) {
        Token token = new Token();
        token.setUserid(user.getUserID());
        token.setCreationTime(LocalDateTime.now());
        token.setNumericCode(randomCode(6));
        saveToken(token);
        return token;
    }

    private String randomCode(float length) {
        char[] NUMERIC = "0123456789".toCharArray();

        StringBuilder string = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(NUMERIC.length);

            string.append(NUMERIC[index]);
        }
        return string.toString();
    }

    public void saveToken(Token token) {
        List<Token> tokensToRemove = ITokenRepository.findAllByUserid(token.getUserid());
        ITokenRepository.deleteAll(tokensToRemove);
        token.setId(token.getUserid());
        ITokenRepository.save(token);
    }

    public boolean verifyToken(String linkHash, String email) {
        User user = userRepository.findByMailAddress(email);
        Token officialToken = ITokenRepository.findByUserid(user.getUserID());
        if (officialToken != null) {
            if (officialToken.getNumericCode().equals(linkHash)) {
                if (officialToken.getCreationTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
                    ITokenRepository.delete(officialToken);
                    return true;
                }
            }
        }
        return false;
    }

    private String HashPassword(String password) {
        int saltLength = 16;
        int hashLength = 32;
        int parallelism = 8;
        int memory = 65536;
        int iterations = 4;

        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);

        return encoder.encode(password);
    }

    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder();
        return encoder.matches(rawPassword, hashedPassword);
    }


}
