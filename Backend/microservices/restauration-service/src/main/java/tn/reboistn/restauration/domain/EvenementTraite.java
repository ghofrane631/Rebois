package tn.reboistn.restauration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "evenement_traite")
public class EvenementTraite {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "traite_le", nullable = false)
    private LocalDateTime traiteLe = LocalDateTime.now();

    protected EvenementTraite() {
    }

    public EvenementTraite(String eventId) {
        this.eventId = eventId;
    }
}