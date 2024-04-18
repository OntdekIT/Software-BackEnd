package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.user.endpoint.userDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

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

    private boolean Admin;

    private String password;

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


    public User(userDto dto){
        this.userID = dto.getId();
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
    }
}
