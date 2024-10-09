package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.RegisterDto;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.regex.Pattern;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userID;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String mailAddress;

    private Boolean Admin = false;

    private String password;

    @OneToMany(mappedBy = "user")
    private Set<Station> stations;

    public User(Long id, String firstName, String lastName, String mailAddress, boolean Admin, String password) {
        this.userID = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mailAddress = mailAddress;
        this.Admin = Admin;
        this.password = password;
    }

    //register
    public User(String mailAddress, String firstName, String lastName, String password) {
        this.mailAddress = mailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }


    public User(String mailAddress, Long id, String password) {
        this.userID = id;
        this.mailAddress = mailAddress;
        this.password = password;
    }


    public User(UserDto dto){
        this.userID = dto.getId();
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
        this.mailAddress = dto.getMailAddress();
        this.Admin = dto.getAdmin();
        this.password = dto.getPassword();
    }

    public boolean ValidateInput(RegisterDto registerDto) throws Exception {
        if(registerDto.getFirstName().isEmpty() || registerDto.getLastName().isEmpty() || registerDto.getMailAddress().isEmpty() || registerDto.getPassword().isEmpty() || registerDto.getConfirmPassword().isEmpty() || registerDto.getMeetstationCode() == null)
        {
            throw new Exception("Please fill out all fields");
        }
        else if (registerDto.getFirstName().length() < 2 || registerDto.getFirstName().length() > 30) {
            throw new Exception("First name has to be between 2 and 30 characters");
        }
        else if (!registerDto.getFirstName().matches("^[A-Za-z0-9]*$")) { //A-Z checks uppercase, a-z checks lowercase, 0-9 checks numbers, ^ start string, $ end string, * zero or more occurences (basically it cant contain it)
            throw new Exception("First name can only include letters and numbers");
        }
        else if (registerDto.getLastName().length() < 2 || registerDto.getLastName().length() > 30) {
            throw new Exception("Last name has to be between 2 and 30 characters");
        }
        else if (!registerDto.getLastName().matches("^[A-Za-z0-9]*$")) {
            throw new Exception("Last name can only include letters and numbers");
        }

        else if (registerDto.getMailAddress().length() < 6 || registerDto.getMailAddress().length() > 60) {
            throw new Exception("Email address has to be between 6 and 60 characters");
        }

        else if(!checkMailAddress(registerDto.getMailAddress()))
        {
            throw new Exception("Invalid email address");
        }

        else if (registerDto.getPassword().length() < 8 || registerDto.getPassword().length() > 50) {
            throw new Exception("Password has to be between 8 and 50 characters");
        }

        else if(!checkPassword(registerDto.getPassword()))
        {
            throw new Exception("Password has to contain a lowercase letter, uppercase letter and a number");
        }

        else if(!Objects.equals(registerDto.getPassword(), registerDto.getConfirmPassword()))
        {
            throw new Exception("Password and confirmation password do not match");
        }

        else if(registerDto.getMeetstationCode().toString().length() != 3) {
          throw new Exception("Code has to contain 3 characters");
        }

        return true;
    }

    public boolean checkPassword (String password) throws Exception {
        char ch;
        boolean containsNumber = false;
        boolean containsUppercase = false;
        boolean containsLowercase = false;

        for(int i=0;i < password.length();i++) {
            ch = password.charAt(i);
            if (Character.isDigit(ch)) {
                containsNumber = true;
            }
            else if (Character.isUpperCase(ch)) {
                containsUppercase = true;
            }
            else if (Character.isLowerCase(ch)) {
                containsLowercase = true;
            }
        }

        return containsLowercase && containsNumber && containsUppercase;
    }

    public static boolean checkMailAddress(String mailAddress) { //https://www.baeldung.com/java-email-validation-regex#strict-regular-expression-validation
        return Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
                .matcher(mailAddress)
                .matches();
    }

    public UserDto toDto(){
        Set<StationDto> stationDtos = new HashSet<StationDto>();
        for(Station station : stations){
            stationDtos.add(station.toDto());
        }
        return new UserDto(userID, firstName, lastName, mailAddress, Admin, stationDtos);
    }
}
