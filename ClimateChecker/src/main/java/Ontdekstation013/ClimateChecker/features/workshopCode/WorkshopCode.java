package Ontdekstation013.ClimateChecker.features.workshopCode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "workshopcode")
@Getter
@Setter
@NoArgsConstructor
public class WorkshopCode {


    @Id
    @Column(name = "code")
    private Long code;

    @NotBlank
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    public WorkshopCode (Long code, LocalDateTime expirationDate) {
        this.code = code;
        this.expirationDate = expirationDate;
    }
}
