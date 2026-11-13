package tn.esprit.ffw.risque.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seuil_risque")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SeuilRisque {
    @Id @Enumerated(EnumType.STRING)
    private NiveauRisque niveau;
    @Column(nullable = false) private int valeurMin;
}
