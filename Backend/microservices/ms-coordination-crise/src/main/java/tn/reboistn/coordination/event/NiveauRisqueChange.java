package tn.reboistn.coordination.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Consommé. Publié par Risque quand le niveau d'un secteur change : alerte de vigilance. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NiveauRisqueChange(
        String eventId,
        Long secteurId,
        String gouvernorat,
        String ancienNiveau,
        String nouveauNiveau) {
}
