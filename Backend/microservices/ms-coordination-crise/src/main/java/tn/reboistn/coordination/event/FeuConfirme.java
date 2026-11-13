package tn.reboistn.coordination.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Consommé. Publié par Signalement quand un garde confirme un feu : ouvre un incident. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FeuConfirme(
        String eventId,
        @JsonAlias("feuId") Long signalementId,
        Long secteurId,
        String gouvernorat,
        Double latitude,
        Double longitude,
        Integer intensite,
        String niveauRisque) {
}
