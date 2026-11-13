package tn.esprit.forestfirewatch.ressources.event;

/**
 * Événements RabbitMQ échangés par Ressources.
 * Chaque événement porte un eventId : un doublon est ignoré.
 */
public final class Evenements {

    private Evenements() {
    }

    /** Consommé. Publié par Risque quand le niveau d'un secteur change. */
    public record NiveauRisqueChange(String eventId, Long secteurId, String gouvernorat,
                                     Double latitude, Double longitude,
                                     String ancienNiveau, String nouveauNiveau) {
    }

    /** Consommé. Publié par Coordination de crise à la clôture d'un incident. */
    public record InterventionTerminee(String eventId, Long incidentId, Long secteurId, Double surfaceBruleeHa) {
    }

    /** Publié par Ressources quand une demande ne peut pas être satisfaite. */
    public record MoyensInsuffisants(Long incidentId, Long secteurId, String gouvernorat,
                                     String typeMoyen, int demandes, int disponibles, String motif) {
    }
}
