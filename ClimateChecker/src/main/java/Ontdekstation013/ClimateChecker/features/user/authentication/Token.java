package Ontdekstation013.ClimateChecker.features.user.authentication;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Token {
    @Id
    private Long id;

    private long userId;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private String numericCode;

    private LocalDateTime creationTime;

    public Token() {
    }
}
