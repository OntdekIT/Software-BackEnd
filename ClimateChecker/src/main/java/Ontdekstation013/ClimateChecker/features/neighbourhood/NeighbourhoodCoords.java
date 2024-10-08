package Ontdekstation013.ClimateChecker.features.neighbourhood;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "region_cords")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NeighbourhoodCoords {
    @Id
    private long id;
    private float latitude;
    private float longitude;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Neighbourhood neighbourhood;
}
