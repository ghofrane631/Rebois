package tn.esprit.ffw.risque.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/** Événement CONSOMMÉ par ms-risque. eventId (UUID) sert à l'idempotence. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InterventionTerminee(String eventId, Long feuId, Long secteurId) { }
