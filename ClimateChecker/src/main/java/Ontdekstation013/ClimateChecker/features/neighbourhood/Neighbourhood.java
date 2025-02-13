package Ontdekstation013.ClimateChecker.features.neighbourhood;

import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "region")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Neighbourhood {
    @Id
    private long id;
    private String name;

    @OneToMany(mappedBy = "neighbourhood")
    private List<NeighbourhoodCoords> coordinates;

    public Neighbourhood(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
