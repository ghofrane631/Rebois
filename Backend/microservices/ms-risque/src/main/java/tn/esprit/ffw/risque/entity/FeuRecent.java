package tn.esprit.ffw.risque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** Copie locale minimale des feux confirmés, pour calculer le bonus « feux récents ». */
@Entity
@Table(name = "feu_recent", indexes = @Index(name = "idx_feu_secteur", columnList = "secteurId,dateConfirmation"))
@Getter @Setter @NoArgsConstructor
public class FeuRecent {
    @Id private Long feuId;
    @Column(nullable = false) private Long secteurId;
    @Column(nullable = false) private LocalDateTime dateConfirmation;
    private boolean eteint;
}
