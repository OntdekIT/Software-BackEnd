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

@Service
public class UserService {

    private final IUserRepository userRepository;
    private final IStationRepository stationRepository;
    private final ITokenRepository ITokenRepository;
    private final WorkshopCodeService adminService;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JWTService jwtService;
    private final WorkshopCodeService workshopCodeService;


    private final UserConverter userConverter;

    @Autowired
    public UserService(IUserRepository userRepository, IStationRepository stationRepository, ITokenRepository ITokenRepository, WorkshopCodeService adminService, JWTService jwtService, WorkshopCodeService workshopCodeService) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.ITokenRepository = ITokenRepository;
        this.adminService = adminService;
        this.workshopCodeService = workshopCodeService;
        this.userConverter = new UserConverter();
        this.jwtService = jwtService;
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
        User user = userRepository.findById(id).get();
        return user.toDto();
    }


    // not yet functional
    public List<UserDataDto> getAllUsers() {
        List<User> userList = userRepository.findAll();
        List<UserDataDto> newDtoList = new ArrayList<>();

        for (User user: userList) {
            newDtoList.add(userConverter.userToUserDataDto(user));
        }

        return newDtoList;
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

    public User login(LoginDto loginDto) throws Exception {
        User user = userRepository.findByEmail(loginDto.email());
        if (verifyPassword(loginDto.password(), user.getPassword())){
            return userRepository.findByEmail(loginDto.email());
        }
        throw new NotFoundException("User not found");
    }

    public UserDto getUserByMail(String mail) {
        ModelMapper mapper = new ModelMapper();
        UserDto dto = new UserDto();
        UserConverter converter = new UserConverter();
        dto = converter.userToUserDto(userRepository.findByEmail(mail));
        //dto = new userDto(mail);
         return dto;
    }


    public ResponseCookie createCookie(User user) {
        Cookie jwtTokenCookie = new Cookie("user-id", user.getUserId().toString());
        ResponseCookie springCookie = ResponseCookie.from(jwtTokenCookie.getName(), jwtTokenCookie.getValue())
                .httpOnly(true)
                .sameSite("None")
                .path("/")
                .build();

        return springCookie;
    }

    public Long grantUserAdmin(UserDto dto) {
        User updatedUser = new User(dto);
        return userRepository.save(updatedUser).getUserId();
    }

    public Token createVerifyToken(User user){
        Token token = new Token();
        token.setUserid(user.getUserId());
        token.setCreationTime(LocalDateTime.now());
        token.setNumericCode(randomCode(6));
        saveToken(token);
        return token;
    }

    private String randomCode(float length){
        char[] NUMERIC ="0123456789".toCharArray();

        StringBuilder string = new StringBuilder();

        Random random = new Random();

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(NUMERIC.length);

            string.append(NUMERIC[index]);
        }
        return string.toString();
    }

    public void saveToken(Token token){
        List<Token> tokensToRemove = ITokenRepository.findAllByUserid(token.getUserid());
        ITokenRepository.deleteAll(tokensToRemove);
        token.setId(token.getUserid());
        ITokenRepository.save(token);
    }

    public boolean verifyToken(String linkHash, String email) {
        User user = userRepository.findByEmail(email);
        Token officialToken = ITokenRepository.findByUserid(user.getUserId());
        if (officialToken != null){
            if (officialToken.getNumericCode().equals(linkHash)) {
                if (officialToken.getCreationTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
                    ITokenRepository.delete(officialToken);
                    return true;
                }
            }
        }
        return false;
    }

    private String HashPassword(String password)
    {
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
