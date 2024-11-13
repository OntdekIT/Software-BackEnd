package Ontdekstation013.ClimateChecker.features.workshopCode;

import Ontdekstation013.ClimateChecker.features.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.common.aliasing.qual.Unique;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "workshop")
@Getter
@Setter
@NoArgsConstructor
public class Workshop {
    @Id
    @Unique
    @Column
    private Long code;

    @NotBlank
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @NotBlank
    @Column(name = "creation_date")
    private LocalDateTime creationTime;

    @OneToMany(mappedBy = "workshop")
    private List<User> users;

    public Workshop(@Unique Long code, LocalDateTime expirationDate) {
        this.code = code;
        this.expirationDate = expirationDate;
    }
}
