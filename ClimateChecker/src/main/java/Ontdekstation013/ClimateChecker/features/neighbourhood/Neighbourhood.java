package Ontdekstation013.ClimateChecker.features.neighbourhood;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
    List<NeighbourhoodCoords> coordinates;

    public Neighbourhood(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
