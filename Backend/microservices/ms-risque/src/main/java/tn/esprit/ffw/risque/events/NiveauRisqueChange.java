package tn.esprit.ffw.risque.events;

import tn.esprit.ffw.risque.entity.NiveauRisque;
import java.time.LocalDateTime;

/** Événement PUBLIÉ par ms-risque (consommé par Ressources et Coordination de crise). */
public record NiveauRisqueChange(String eventId, Long secteurId, NiveauRisque ancienNiveau,
                                 NiveauRisque nouveauNiveau, int indice, String cause,
                                 LocalDateTime date) { }
