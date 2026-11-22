package com.esprit.ffw.signalement.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Marqueur d'idempotence : un eventId n'est traité qu'une seule fois par consommateur. */
@Data @NoArgsConstructor
@Document(collection = "processed_events")
public class ProcessedEvent {
    @Id
    private String eventId;
    private String consommateur;
    private Instant traiteLe;

    public ProcessedEvent(String eventId, String consommateur, Instant traiteLe) {
        this.eventId = eventId;
        this.consommateur = consommateur;
        this.traiteLe = traiteLe;
    }
}
