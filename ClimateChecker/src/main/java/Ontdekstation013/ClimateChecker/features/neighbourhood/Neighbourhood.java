package Ontdekstation013.ClimateChecker.features.neighbourhood;

import Ontdekstation013.ClimateChecker.features.location.Location;
import Ontdekstation013.ClimateChecker.features.station.Measurement;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "region")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Neighbourhood {
    @Id
    private long id;
    private String name;
}
