package tn.reboistn.coordination.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bilan d'un incident : au plus un par incident, il porte la même clé que l'incident. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bilan {

    @Id
    private Long incidentId;

    @JsonIgnore
    @MapsId
    @OneToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    private Double surfaceBruleeHa;

    private Integer dureeMin;

    private String cause;
}
