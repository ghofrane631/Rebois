package tn.esprit.ffw.risque.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** Table d'idempotence : un eventId n'est traité qu'une seule fois. */
@Entity
@Table(name = "evenement_traite")
@Getter @Setter @NoArgsConstructor
public class EvenementTraite {
    @Id private String eventId;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private LocalDateTime traiteLe;

    public EvenementTraite(String eventId, String type) {
        this.eventId = eventId; this.type = type; this.traiteLe = LocalDateTime.now();
    }
}
