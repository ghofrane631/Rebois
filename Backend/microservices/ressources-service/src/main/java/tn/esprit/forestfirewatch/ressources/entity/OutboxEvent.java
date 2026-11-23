package tn.esprit.forestfirewatch.ressources.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Outbox : l'événement est écrit dans la même transaction que la donnée,
 * puis publié dans RabbitMQ par une tâche planifiée. Aucun événement n'est perdu
 * si RabbitMQ est indisponible au moment de l'écriture.
 */
@Entity
@Table(name = "outbox_event", indexes = {
        @Index(name = "idx_outbox_publie", columnList = "publie, cree_le")
})
public class OutboxEvent {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(nullable = false, length = 40)
    private String type;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe;

    @Column(nullable = false)
    private boolean publie = false;

    public OutboxEvent() {
    }

    public OutboxEvent(String eventId, String type, String payload) {
        this.eventId = eventId;
        this.type = type;
        this.payload = payload;
        this.creeLe = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public String getType() { return type; }
    public String getPayload() { return payload; }
    public LocalDateTime getCreeLe() { return creeLe; }
    public boolean isPublie() { return publie; }
    public void marquerPublie() { this.publie = true; }
}
