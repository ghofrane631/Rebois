package tn.esprit.forestfirewatch.ressources.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Identifiant d'un événement RabbitMQ déjà traité.
 * Si le même événement arrive deux fois, le doublon est ignoré (consommateur idempotent).
 */
@Entity
@Table(name = "evenement_traite")
public class EvenementTraite {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(name = "traite_le", nullable = false)
    private LocalDateTime traiteLe;

    public EvenementTraite() {
    }

    public EvenementTraite(String eventId, String type) {
        this.eventId = eventId;
        this.type = type;
        this.traiteLe = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public String getType() { return type; }
    public LocalDateTime getTraiteLe() { return traiteLe; }
}
