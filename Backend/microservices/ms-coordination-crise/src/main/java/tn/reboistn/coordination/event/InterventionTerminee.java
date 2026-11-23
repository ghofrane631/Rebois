package tn.reboistn.coordination.event;

import java.time.LocalDateTime;

/** Publié à la clôture d'un incident (consommé par Forêt, Signalement, Risque, Ressources et Restauration). */
public record InterventionTerminee(
        String eventId,
        Long incidentId,
        Long signalementId,
        Long secteurId,
        Double surfaceBruleeHa,
        LocalDateTime date) {
}
