package tn.reboistn.coordination.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Consommé. Publié par Ressources quand une demande de moyens ne peut pas être satisfaite. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MoyensInsuffisants(
        String eventId,
        Long incidentId,
        String gouvernorat,
        String typeMoyen,
        Integer demandes,
        Integer disponibles) {
}
