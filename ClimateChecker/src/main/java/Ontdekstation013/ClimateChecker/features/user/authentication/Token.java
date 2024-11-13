package Ontdekstation013.ClimateChecker.features.user.authentication;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
