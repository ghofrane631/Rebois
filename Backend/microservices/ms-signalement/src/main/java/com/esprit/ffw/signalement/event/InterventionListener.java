package com.esprit.ffw.signalement.event;

import com.esprit.ffw.signalement.config.RabbitConfig;
import com.esprit.ffw.signalement.service.SignalementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterventionListener {
    private final SignalementService service;

    @RabbitListener(queues = RabbitConfig.QUEUE_INTERVENTION)
    public void onInterventionTerminee(InterventionTermineeEvent event) {
        log.info("InterventionTerminee reçu eventId={} incidentId={}", event.eventId(), event.incidentId());
        service.cloreApresIntervention(event.signalementId(), event.secteurId()); // idempotent
    }
}
