package Ontdekstation013.ClimateChecker.features.user;
import Ontdekstation013.ClimateChecker.exception.ExistingUniqueIdentifierException;
import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.authentication.JWTService;
import Ontdekstation013.ClimateChecker.features.authentication.Token;
import Ontdekstation013.ClimateChecker.features.authentication.TokenRepository;
import Ontdekstation013.ClimateChecker.features.authentication.endpoint.loginDto;
import Ontdekstation013.ClimateChecker.features.authentication.endpoint.registerDto;
import Ontdekstation013.ClimateChecker.features.meetstation.Meetstation;
import Ontdekstation013.ClimateChecker.features.meetstation.MeetstationRepository;
import Ontdekstation013.ClimateChecker.features.user.endpoint.editUserDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDataDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDto;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    private final UserRepository userRepository;
    private final MeetstationRepository meetstationRepository;
    private final TokenRepository tokenRepository;
    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JWTService jwtService;


    private final UserConverter userConverter;

    @Autowired
    public UserService(UserRepository userRepository, MeetstationRepository meetstationRepository, TokenRepository tokenRepository, JWTService jwtService) {
        this.userRepository = userRepository;
        this.meetstationRepository = meetstationRepository;
        this.tokenRepository = tokenRepository;
        this.userConverter = new UserConverter();
        this.jwtService = jwtService;
    }

    public userDto findUserById(long id) {
        User user = userRepository.findById(id).get();
        return user.toDto();
    }


    // not yet functional
    public List<userDataDto> getAllUsers() {
        List<User> userList = userRepository.findAll();
        List<userDataDto> newDtoList = new ArrayList<>();

        for (User user: userList) {
            newDtoList.add(userConverter.userToUserDataDto(user));
        }

        return newDtoList;
    }

    // not yet functional
    // why?
    public List<userDataDto> getAllByPageId(long pageId) {
        List<userDataDto> newDtoList = new ArrayList<userDataDto>();

        return newDtoList;
    }

    public User editUser(editUserDto editUserDto) throws Exception {
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

    public userDto deleteUser(Long id) {
        User user = userRepository.getById(id);
        userRepository.deleteById(id);
        return (userConverter.userToUserDto(user));
    }

    public User createNewUser(registerDto registerDto) throws Exception {
        try
        {
            User user = new User();
            if(user.ValidateInput(registerDto)) {
                registerDto.setMailAddress(registerDto.getMailAddress().toLowerCase());
                if (!userRepository.existsUserByMailAddress(registerDto.getMailAddress())) { //check if email is unique
                    user = new User(registerDto.getMailAddress(), registerDto.getFirstName(), registerDto.getLastName(), registerDto.getPassword());
                }
                else {
                    throw new ExistingUniqueIdentifierException("Email already in use");
                }
            }
            else {
                throw new InvalidArgumentException("Invalid information");
            }

            user.setPassword(HashPassword(user.getPassword()));

            Meetstation meetstation = meetstationRepository.getByRegistrationCode(registerDto.getMeetstationCode().longValue());
            if(meetstation != null)
            {
                if(meetstation.getUserid() == null)
                {
                    user = userRepository.save(user);
                    meetstation.setUserid(user.getUserID());
                    return user;
                }

                throw new Exception("Meetstation is al in gebruik");
            }

            throw new Exception("Meetstation bestaat niet");


        }

        catch (Exception ex) {
            throw ex;
        }

    }

    public User login(loginDto loginDto) throws Exception {
        User user = userRepository.findByMailAddress(loginDto.getMailAddress());
        if (verifyPassword(loginDto.getPassword(), user.getPassword())){
            return userRepository.findByMailAddress(loginDto.getMailAddress());
        }
        throw new NotFoundException("User not found");
    }

    public userDto getUserByMail(String mail) {
        ModelMapper mapper = new ModelMapper();
        userDto dto = new userDto();
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

    public Token createVerifyToken(User user){
        Token token = new Token();
        token.setUserid(user.getUserID());
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
        List<Token> tokensToRemove = tokenRepository.findAllByUserid(token.getUserid());
        tokenRepository.deleteAll(tokensToRemove);
        token.setId(token.getUserid());
        tokenRepository.save(token);
    }

    public boolean verifyToken(String linkHash, String email) {
        User user = userRepository.findByMailAddress(email);
        Token officialToken = tokenRepository.findByUserid(user.getUserID());
        if (officialToken != null){
            if (officialToken.getNumericCode().equals(linkHash)) {
                if (officialToken.getCreationTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
                    tokenRepository.delete(officialToken);
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
