package Ontdekstation013.ClimateChecker.features.workshopCode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.common.aliasing.qual.Unique;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "workshopcode")
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

    public Workshop(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Workshop(@Unique Long code, LocalDateTime expirationDate) {
        this.code = code;
        this.expirationDate = expirationDate;
    }

    public Workshop(@Unique Long code, LocalDateTime expirationDate, LocalDateTime creationTime) {
        this.code = code;
        this.expirationDate = expirationDate;
        this.creationTime = creationTime;
    }
}
