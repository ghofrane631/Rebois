package tn.reboistn.coordination.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Événement RabbitMQ déjà traité : un message reçu deux fois n'est appliqué qu'une fois. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class EvenementTraite {

    @Id
    private String eventId;

    private String type;

    private LocalDateTime traiteLe;

    public EvenementTraite(String eventId, String type) {
        this.eventId = eventId;
        this.type = type;
        this.traiteLe = LocalDateTime.now();
    }
}
