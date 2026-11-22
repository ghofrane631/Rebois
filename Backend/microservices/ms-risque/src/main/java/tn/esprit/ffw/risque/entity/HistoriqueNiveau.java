package tn.esprit.ffw.risque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_niveau", indexes = @Index(name = "idx_hist_secteur", columnList = "secteurId,date"))
@Getter @Setter @NoArgsConstructor
public class HistoriqueNiveau {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long secteurId;
    @Enumerated(EnumType.STRING) private NiveauRisque ancien;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NiveauRisque nouveau;
    private int indice;
    @Column(nullable = false) private String cause;
    @Column(nullable = false) private LocalDateTime date;
}
