package Ontdekstation013.ClimateChecker.features.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import Ontdekstation013.ClimateChecker.features.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "workshopcode")
@Getter
@Setter
@NoArgsConstructor
public class WorkshopCode {


    @Id
    @Column(name = "workshopcode")
    private String randomCode;

    @NotBlank
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    public WorkshopCode (String code, LocalDateTime expirationDate) {
        this.randomCode = code;
        this.expirationDate = expirationDate;
    }
}



//public User(Long id, String firstName, String lastName, String mailAddress, boolean Admin, String password) {
//    this.userID = id;
//    this.firstName = firstName;
//    this.lastName = lastName;
//    this.mailAddress = mailAddress;
//    this.Admin = Admin;
//    this.password = password;
//}
