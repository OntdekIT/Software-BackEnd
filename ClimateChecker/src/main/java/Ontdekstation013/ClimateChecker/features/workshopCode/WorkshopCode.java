package Ontdekstation013.ClimateChecker.features.workshopCode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "workshopcode")
@Getter
@Setter
@NoArgsConstructor
public class WorkshopCode {


    @Id
    @Unique
    @Column
    private Long code;

    @NotBlank
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    public WorkshopCode(Long code, LocalDateTime expirationDate) {
        this.code = code;
        this.expirationDate = expirationDate;
    }
}
