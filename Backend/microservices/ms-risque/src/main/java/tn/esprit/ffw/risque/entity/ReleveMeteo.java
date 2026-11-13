package tn.esprit.ffw.risque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "releve_meteo", indexes = @Index(name = "idx_releve_secteur_date", columnList = "secteurId,dateReleve"))
@Getter @Setter @NoArgsConstructor
public class ReleveMeteo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long secteurId;
    @Column(nullable = false) private LocalDateTime dateReleve;
    @Column(nullable = false) private double temperature;
    @Column(nullable = false) private double humidite;
    @Column(nullable = false) private double vent;
    @Column(nullable = false) private int joursSansPluie;
    private String saisiPar;
}
