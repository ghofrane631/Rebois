package tn.esprit.ffw.risque.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tn.esprit.ffw.risque.events.*;
import tn.esprit.ffw.risque.service.RisqueService;

/** Consommation ASYNCHRONE des événements des autres services. */
@Component
@RequiredArgsConstructor
public class RisqueEventConsumer {

    private final RisqueService service;

    @RabbitListener(queues = RabbitNames.Q_SECTEUR_CREE)
    public void onSecteurCree(SecteurCree e) { service.traiter(e); }

    @RabbitListener(queues = RabbitNames.Q_FEU_CONFIRME)
    public void onFeuConfirme(FeuConfirme e) { service.traiter(e); }

    @RabbitListener(queues = RabbitNames.Q_INTERVENTION_TERMINEE)
    public void onInterventionTerminee(InterventionTerminee e) { service.traiter(e); }

    @RabbitListener(queues = RabbitNames.Q_ZONE_RESTAUREE)
    public void onZoneRestauree(ZoneRestauree e) { service.traiter(e); }
}
