package tn.reboistn.restauration.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tn.reboistn.restauration.service.RestaurationService;

@Component
public class InterventionTermineeListener {

    private final RestaurationService restaurationService;

    public InterventionTermineeListener(RestaurationService restaurationService) {
        this.restaurationService = restaurationService;
    }

    @RabbitListener(queues = "${app.events.intervention-terminee-queue}")
    public void onInterventionTerminee(InterventionTermineeEvent event) {
        restaurationService.createZoneFromIntervention(event);
    }
}