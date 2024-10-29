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
    private String numericCode;
    private LocalDateTime creationTime;

    @Id
    private Long id;

    private long userId;

    public Token() {
    }
}
