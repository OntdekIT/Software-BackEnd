package Ontdekstation013.ClimateChecker.features.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userID;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String mailAddress;

    private boolean Admin;

    public User(long id, String firstName, String lastName, String mailAddress, boolean Admin) {
        this.userID = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mailAddress = mailAddress;
        this.Admin = Admin;
    }

    //register
    public User(String mailAddress, String firstName, String lastName) {
        this.mailAddress = mailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
    }


    public User(String mailAddress, Long id) {
        this.userID = id;
        this.mailAddress = mailAddress;
    }
}
