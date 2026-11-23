package tn.reboistn.coordination.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Consommé. Publié par User à la création d'un profil : abonnement par défaut à sa zone. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UtilisateurCree(
        String eventId,
        String userId,
        String role,
        String gouvernorat) {
}
