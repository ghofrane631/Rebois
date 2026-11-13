package tn.esprit.forestfirewatch.ressources.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tn.esprit.forestfirewatch.ressources.event.Evenements.InterventionTerminee;
import tn.esprit.forestfirewatch.ressources.event.Evenements.NiveauRisqueChange;
import tn.esprit.forestfirewatch.ressources.service.EvenementService;

/** Consommateurs RabbitMQ : la logique est dans EvenementService. */
@Component
public class EvenementListener {

    private final EvenementService evenementService;

    public EvenementListener(EvenementService evenementService) {
        this.evenementService = evenementService;
    }

    @RabbitListener(queues = RabbitConfig.Q_NIVEAU_RISQUE)
    public void niveauRisqueChange(NiveauRisqueChange evenement) {
        evenementService.surNiveauRisqueChange(evenement);
    }

    @RabbitListener(queues = RabbitConfig.Q_INTERVENTION_TERMINEE)
    public void interventionTerminee(InterventionTerminee evenement) {
        evenementService.surInterventionTerminee(evenement);
    }
}
