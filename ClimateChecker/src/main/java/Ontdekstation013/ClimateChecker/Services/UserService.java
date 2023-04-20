package Ontdekstation013.ClimateChecker.Services;
import Ontdekstation013.ClimateChecker.exception.BadRequestException;
import Ontdekstation013.ClimateChecker.exception.ConflictException;
import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.models.Token;
import Ontdekstation013.ClimateChecker.models.User;
import Ontdekstation013.ClimateChecker.models.dto.*;
import Ontdekstation013.ClimateChecker.repositories.TokenRepository;
import Ontdekstation013.ClimateChecker.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private PasswordEncoder encoder = new BCryptPasswordEncoder();

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
        newdto.setUserName(user.getUserName());
        newdto.setMailAddress(user.getMailAddress());
        newdto.setFirstName(user.getFirstName());
        newdto.setLastName(user.getLastName());
        newdto.setUserName(user.getUserName());
        return newdto;
    }

    public userDto userToUserDto (User user){
        userDto newdto = new userDto();
        newdto.setId(user.getUserID());
        newdto.setMailAddress(user.getMailAddress());
        newdto.setLastName(user.getLastName());
        newdto.setFirstName(user.getFirstName());
        newdto.setUserName(user.getUserName());
        return newdto;
    }

    // not yet functional
    public List<userDataDto> getAllUsers() {
        List<User> userList = userRepository.findAll();
        List<userDataDto> newDtoList = new ArrayList<>();

        for (User user: userList) {
            newDtoList.add(userToUserDataDto(user));
        }

        return newDtoList;
    }

    // not yet functional
    public List<userDataDto> getAllByPageId(long pageId) {
        List<userDataDto> newDtoList = new ArrayList<userDataDto>();

        return newDtoList;
    }

    public User editUser(editUserDto editUserDto) throws Exception {
        User user = userRepository.findById(editUserDto.getId()).orElseThrow();

        if (editUserDto.getFirstName().length() < 256)
            user.setFirstName(editUserDto.getFirstName());
        else
            throw new BadRequestException(null); //First name can't be longer than 256 characters

        if (editUserDto.getLastName().length() < 256)
            user.setLastName(editUserDto.getLastName());
        else
            throw new BadRequestException(null); //Last name can't be longer than 256 characters


        if (editUserDto.getUserName().length() < 256) {
            if (!editUserDto.getUserName().equals(user.getUserName())) {
                if (!userRepository.existsUserByUserName(editUserDto.getUserName())) {
                    user.setUserName(editUserDto.getUserName());
                } else {
                    throw new ConflictException(1); //Username previously used
                }
            }
        } else {
            throw new BadRequestException(null); //Last name can't be longer than 256 characters
        }

        editUserDto.setMailAddress(editUserDto.getMailAddress().toLowerCase());
        if (!user.getMailAddress().equals(editUserDto.getMailAddress())) {
            if (editUserDto.getMailAddress().contains("@")) {
                if (!userRepository.existsUserByMailAddress(editUserDto.getMailAddress())) {
                    String mail = user.getMailAddress();
                    user.setMailAddress(editUserDto.getMailAddress());
                    userRepository.save(user);
                    user.setMailAddress(mail);
                } else {
                    throw new ConflictException(2); //Email already previously used
                }
            } else {
                throw new BadRequestException(null); //Invalid email address
            }
        }

        return user;
    }

    public userDto deleteUser(long id) {
        User user = userRepository.getById(id);
        userRepository.deleteById(id);
        return (userToUserDto(user));
    }

    public User createNewUser(registerDto registerDto) throws Exception {
        User user = new User();
        if (registerDto.getFirstName().length() > 256) {
            throw new BadRequestException(null); //First name can't be longer than 256 characters
        }
        if (registerDto.getLastName().length() > 256) {
            throw new BadRequestException(null); //Last name can't be longer than 256 characters
        }

        if (registerDto.getUserName().length() < 256) {
            if (!userRepository.existsUserByUserName(registerDto.getUserName())) {
                user.setUserName(registerDto.getUserName());
            } else {
                throw new ConflictException(1); //Username previously used
            }
        } else {
            throw new BadRequestException(null); //Last name can't be longer than 256 characters
        }

        registerDto.setMailAddress(registerDto.getMailAddress().toLowerCase());
        if (registerDto.getMailAddress().contains("@")) {
            if (!userRepository.existsUserByMailAddress(registerDto.getMailAddress())) {
                user = new User(registerDto.getMailAddress(), registerDto.getFirstName(), registerDto.getLastName(), registerDto.getUserName());
            } else {
                throw new ConflictException(2); //Username previously used
            }
        } else {
            throw new BadRequestException(null); //invalid email adress
        }

        user = userRepository.save(user);
        return user;
    }

    public User verifyMail(loginDto loginDto) throws Exception {
        User user = userRepository.findByMailAddress(loginDto.getMailAddress());
        if (user == null){
            throw new NotFoundException(null); //user not found
        }
        return user;
    }
    public userDto getUserByMail(String mail) {
        ModelMapper mapper = new ModelMapper();
        userDto dto = new userDto();
        mapper.map(userRepository.findByMailAddress(mail), dto);
         return dto;
    }


    public Token createToken(User user) {
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
        return (domain + "Verify/" + token.getLinkHash() + "/" + token.getUser().getMailAddress());
    }

    public String createLink(Token token, String newEmail){ //for changing to new email address
        String domain = "http://localhost:8082/";
        return (domain + "api/User/verify" + "?linkHash=" + token.getLinkHash() + "&oldEmail=" + token.getUser().getMailAddress() + "&newEmail=" + newEmail);
    }


}
