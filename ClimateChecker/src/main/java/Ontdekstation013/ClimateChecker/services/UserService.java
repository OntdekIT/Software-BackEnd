package Ontdekstation013.ClimateChecker.services;
import Ontdekstation013.ClimateChecker.models.Station;
import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.Token;
import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.repositories.TokenRepository;
import Ontdekstation013.ClimateChecker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Autowired
    public UserService(UserRepository userRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    public userDto findUserById(long id) {
        User user = userRepository.findById(id).get();
        userDto newdto = userToUserDto(user);
        return newdto;
    }

    public userDataDto userToUserDataDto (User user){
        userDataDto newdto = new userDataDto();
        newdto.setId(user.getUserID());
        newdto.setMailAddress(user.getMailAddress());
        newdto.setFirstName(user.getFirstName());
        newdto.setLastName(user.getLastName());
        return newdto;
    }

    public userDto userToUserDto (User user){
        userDto newdto = new userDto();
        newdto.setId(user.getUserID());
        newdto.setMailAddress(user.getMailAddress());
        newdto.setUserName(user.getUserName());
        newdto.setLastName(user.getLastName());
        newdto.setFirstName(user.getFirstName());
        return newdto;
    }

    // not yet functional
    public List<userDataDto> getAllUsers() {
        List<User> userList = userRepository.findAll();
        List<userDataDto> newDtoList = new ArrayList<>();

        for (User user: userList
        ) {

            newDtoList.add(userToUserDataDto(user));
        }


        return newDtoList;
    }

    // not yet functional
    public List<userDataDto> getAllByPageId(long pageId) {
        List<userDataDto> newDtoList = new ArrayList<userDataDto>();

        return newDtoList;
    }



    public void deleteUser(long Id) {

    }

    public boolean createNewUser(registerDto registerDto) {
        if (registerDto.getFirstName().length() < 256 && registerDto.getLastName().length() < 256 && registerDto.getUserName().length() < 256) {
            if (registerDto.getMailAddress().contains("@")) {
                if (!userRepository.existsUserByUserNameOrMailAddress(registerDto.getUserName(), registerDto.getMailAddress())) {
                    User user = new User(registerDto.getUserName(), registerDto.getMailAddress(), registerDto.getFirstName(), registerDto.getLastName());
                    userRepository.save(user);
                    System.out.println("Gelukt");
                    return true;
                }
            }
        }
        System.out.println("Niet gelukt");
        return false;
    }

    public boolean verifyMail(loginDto loginDto) {
        return userRepository.findByMailAddress(loginDto.getMailAddress()) != null;
    }
    
/*    public void loginUser(loginDto loginDto) {
        System.out.println(loginDto.getPassword());
    }*/

    public void editUser(editUserDto registerDto) {
        System.out.println("Test edituser");
    }


    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Token createToken(User user){
        Token token = new Token();

        token.setUser(user);
        token.setCreationTime(LocalDateTime.now());
//        token.setLinkHash("abcdefhijk");
        token.setLinkHash(encoder.encode(user.getMailAddress() + user.getUserID()));

        return token;
    }

    public void saveToken(Token token){
        if (tokenRepository.existsByUser(token.getUser())) {
            token.setUser(token.getUser());
            System.out.println("something else");
        }
        token.setId(token.getUser().getUserID());
        tokenRepository.save(token);
    }

    public boolean decryptToken(Token token) {
        User user = userRepository.findById(token.getUser().getUserID()).get();

        if (encoder.matches(user.getMailAddress() + user.getUserID(), token.getLinkHash())) {
            return true;
        }
        return false;
    }
}
