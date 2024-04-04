package Ontdekstation013.ClimateChecker.features.user;
import Ontdekstation013.ClimateChecker.exception.ExistingUniqueIdentifierException;
import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.authentication.JWTService;
import Ontdekstation013.ClimateChecker.features.authentication.Token;
import Ontdekstation013.ClimateChecker.features.authentication.TokenRepository;
import Ontdekstation013.ClimateChecker.features.authentication.endpoint.loginDto;
import Ontdekstation013.ClimateChecker.features.authentication.endpoint.registerDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.editUserDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDataDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDto;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final TokenRepository tokenRepository;
    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JWTService jwtService;


    private final UserConverter userConverter;

    @Autowired
    public UserService(UserRepository userRepository, TokenRepository tokenRepository, JWTService jwtService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.userConverter = new UserConverter();
        this.jwtService = jwtService;
    }

    public userDto findUserById(long id) {
        User user = userRepository.findById(id).get();
        userDto newdto = userConverter.userToUserDto(user);
        return newdto;
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
        User user = new User();
        if (registerDto.getFirstName().length() > 256) {
            throw new InvalidArgumentException("First name can't be longer than 256 characters");
        }
        if (registerDto.getLastName().length() > 256) {
            throw new InvalidArgumentException("Last name can't be longer than 256 characters");
        }

        if (registerDto.getPassword().length() > 256) {
            throw new InvalidArgumentException("Password can't be longer than 256 characters");
        }

        registerDto.setMailAddress(registerDto.getMailAddress().toLowerCase());
        if (registerDto.getMailAddress().contains("@")) {
            if (!userRepository.existsUserByMailAddress(registerDto.getMailAddress())) {
                user = new User(registerDto.getMailAddress(), registerDto.getFirstName(), registerDto.getLastName(), registerDto.getPassword());
            } else {
                throw new ExistingUniqueIdentifierException("Email already in use");
            }
        } else {
            throw new InvalidArgumentException("invalid email address");
        }

        user = userRepository.save(user);
        return user;
    }

    //TODO email en wachtwoord verifyen
    public User login(loginDto loginDto) {
        User user = userRepository.findByMailAddress(loginDto.getMailAddress());
        if (user == null){
            throw new NotFoundException("User not found");
        }
        return user;
    }
    public userDto getUserByMail(String mail) {
        ModelMapper mapper = new ModelMapper();
        userDto dto = new userDto();
        UserConverter converter = new UserConverter();
        dto = converter.userToUserDto(userRepository.findByMailAddress(mail));
        //dto = new userDto(mail);
         return dto;
    }


    public Cookie createCookie(User user) {
        Cookie jwtTokenCookie = new Cookie("user-id", user.getUserID().toString());
        jwtTokenCookie.setMaxAge(6000);
        jwtTokenCookie.setSecure(false);
        jwtTokenCookie.setHttpOnly(true);
        jwtTokenCookie.setPath("/");

        return jwtTokenCookie;
//        UserConverter userConverter = new UserConverter();
//        Token token = new Token();
//        token.setUser(user);
//        token.setCreationTime(LocalDateTime.now());
//        token.setLinkHash(jwtService.createJWT(userConverter.userToUserDto(user)));
//        return token;
    }

    public Token createVerifyToken(User user){
        Token token = new Token();
        token.setUser(user);
        token.setCreationTime(LocalDateTime.now());
        token.setLinkHash(randomString(32));
        return token;
    }

    private String randomString(int length) {
        char[] ALPHANUMERIC ="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

        StringBuilder string = new StringBuilder();

        Random random = new Random();

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHANUMERIC.length);

            string.append(ALPHANUMERIC[index]);
        }
        return string.toString();
    }

    public void saveToken(Token token){
        if (tokenRepository.existsByUser(token.getUser())) {
            token.setUser(token.getUser());
        }
        token.setId(token.getUser().getUserID());
        tokenRepository.save(token);
    }

    public boolean verifyToken(String linkHash, String email) {
        User user = userRepository.findByMailAddress(email);
        Token officialToken = tokenRepository.findByUser(user);
        if (officialToken != null){
            if (officialToken.getLinkHash().equals(linkHash)) {
                if (officialToken.getCreationTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
                    tokenRepository.delete(officialToken);
                    return true;
                }
            }
        }
        return false;
    }


    public boolean verifyToken(String linkHash, String oldEmail, String newEmail) { //for changing to new email address
        User user = userRepository.findByMailAddress(oldEmail);
        Token officialToken = tokenRepository.findByUser(user);
        if (officialToken != null){
            if (officialToken.getLinkHash().equals(linkHash) && encoder.matches(newEmail + user.getUserID(), officialToken.getLinkHash())) {
                if (officialToken.getCreationTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
                    user.setMailAddress(newEmail);
                    userRepository.save(user);
                    tokenRepository.delete(officialToken);
                    return true;
                }
            }
        }
        return false;
    }

    public String createLink(Token token){
        String domain = "http://localhost:3000/";
        String test = domain + "verify" + "?linkHash=" + token.getLinkHash() + "&email=" + token.getUser().getMailAddress();
        return (test);
    }

    public String createLink(Token token, String newEmail){ //for changing to new email address
        String domain = "http://localhost:8082/";
        return (domain + "api/User/verify" + "?linkHash=" + token.getLinkHash() + "&oldEmail=" + token.getUser().getMailAddress() + "&newEmail=" + newEmail);
    }


}
