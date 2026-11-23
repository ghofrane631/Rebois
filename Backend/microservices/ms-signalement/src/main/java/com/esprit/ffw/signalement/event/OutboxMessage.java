package com.esprit.ffw.signalement.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Événement à publier, persisté avant l'envoi RabbitMQ (pattern outbox). */
@Data @NoArgsConstructor
@Document(collection = "outbox")
@CompoundIndex(name = "statut_next_attempt", def = "{'statut': 1, 'nextAttemptAt': 1}")
public class OutboxMessage {
    @Id
    private String id;
    @Indexed(unique = true)
    private String eventId;
    private String type;
    private String routingKey;
    private String payload;              // JSON de l'événement
    private OutboxStatus statut;
    private int tentatives;
    private Instant creeLe;
    private Instant nextAttemptAt;
    private Instant envoyeLe;
    private String derniereErreur;
}
