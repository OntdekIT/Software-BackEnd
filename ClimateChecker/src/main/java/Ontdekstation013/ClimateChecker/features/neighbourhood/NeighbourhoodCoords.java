package Ontdekstation013.ClimateChecker.features.neighbourhood;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "region_coords")
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
