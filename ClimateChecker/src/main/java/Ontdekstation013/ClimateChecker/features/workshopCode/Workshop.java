package Ontdekstation013.ClimateChecker.features.workshopCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.common.aliasing.qual.Unique;

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

    public Workshop(@Unique Long code, LocalDateTime expirationDate) {
        this.code = code;
        this.expirationDate = expirationDate;
    }
}
