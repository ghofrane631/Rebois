package tn.esprit.ffw.risque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "indice_secteur")
@Getter @Setter @NoArgsConstructor
public class IndiceSecteur {
    @Id private Long secteurId;
    private int scoreMeteo;          // 0..100, issu du dernier relevé
    private int feuxRecents;         // feux actifs confirmés dans la fenêtre
    private int ajustement;          // <= 0 : baisse après restauration
    private int indice;              // 0..100 : résultat final
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private NiveauRisque niveau = NiveauRisque.FAIBLE;
    private LocalDateTime majLe;
    @Version private Long version;
}
