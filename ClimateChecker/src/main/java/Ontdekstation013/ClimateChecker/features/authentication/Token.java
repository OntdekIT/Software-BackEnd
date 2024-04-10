package Ontdekstation013.ClimateChecker.features.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import Ontdekstation013.ClimateChecker.features.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Token {
    private String numericCode;
    private LocalDateTime creationTime;

    @Id
    private Long id;
    @OneToOne
    private User user;
}
