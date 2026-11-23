package tn.reboistn.coordination.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tn.reboistn.coordination.event.FeuConfirme;
import tn.reboistn.coordination.event.MoyensInsuffisants;
import tn.reboistn.coordination.event.NiveauRisqueChange;
import tn.reboistn.coordination.event.UtilisateurCree;
import tn.reboistn.coordination.service.EvenementService;

/** Consommateurs RabbitMQ : la logique est dans EvenementService. */
@Component
@RequiredArgsConstructor
public class EvenementListener {

    private final EvenementService evenementService;

    @RabbitListener(queues = RabbitConfig.Q_FEU_CONFIRME)
    public void feuConfirme(FeuConfirme evenement) {
        evenementService.surFeuConfirme(evenement);
    }

    @RabbitListener(queues = RabbitConfig.Q_NIVEAU_RISQUE)
    public void niveauRisqueChange(NiveauRisqueChange evenement) {
        evenementService.surNiveauRisqueChange(evenement);
    }

    @RabbitListener(queues = RabbitConfig.Q_MOYENS_INSUFFISANTS)
    public void moyensInsuffisants(MoyensInsuffisants evenement) {
        evenementService.surMoyensInsuffisants(evenement);
    }

    @RabbitListener(queues = RabbitConfig.Q_UTILISATEUR_CREE)
    public void utilisateurCree(UtilisateurCree evenement) {
        evenementService.surUtilisateurCree(evenement);
    }
}
