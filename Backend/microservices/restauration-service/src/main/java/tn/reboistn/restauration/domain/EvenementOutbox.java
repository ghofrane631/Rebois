package tn.reboistn.restauration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "evenement_outbox")
public class EvenementOutbox {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false, length = 120)
    private String exchangeName;

    @Column(nullable = false, length = 120)
    private String routingKey;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe = LocalDateTime.now();

    @Column(name = "publie_le")
    private LocalDateTime publieLe;

    protected EvenementOutbox() {
    }

    public EvenementOutbox(String eventId, String eventType, String exchangeName,
                           String routingKey, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.payload = payload;
    }

    public String getEventId() { return eventId; }
    public String getExchangeName() { return exchangeName; }
    public String getRoutingKey() { return routingKey; }
    public String getPayload() { return payload; }
    public void markPublished() { this.publieLe = LocalDateTime.now(); }
}