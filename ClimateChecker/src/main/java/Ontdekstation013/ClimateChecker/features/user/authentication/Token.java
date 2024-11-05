package Ontdekstation013.ClimateChecker.features.user.authentication;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Token {
    @Id
    private Long id;

    private long userId;

    private String numericCode;

    private LocalDateTime creationTime;

    public Token() {
    }
}
