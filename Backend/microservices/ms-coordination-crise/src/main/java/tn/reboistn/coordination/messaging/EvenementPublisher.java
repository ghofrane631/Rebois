package tn.reboistn.coordination.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tn.reboistn.coordination.event.InterventionTerminee;

/** Publie les événements de ce service sur l'exchange commun. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EvenementPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${evenements.exchange}")
    private String exchange;

    @Value("${evenements.cles.intervention-terminee}")
    private String cleInterventionTerminee;

    /**
     * Si RabbitMQ est indisponible, la clôture n'est pas annulée : l'échec est journalisé.
     * Prévu en S7 : table outbox pour republier automatiquement.
     */
    public void interventionTerminee(InterventionTerminee evenement) {
        try {
            rabbitTemplate.convertAndSend(exchange, cleInterventionTerminee, evenement);
            log.info("InterventionTerminee publie pour l'incident {}", evenement.incidentId());
        } catch (AmqpException e) {
            log.warn("RabbitMQ indisponible, InterventionTerminee non publie pour l'incident {} : {}",
                    evenement.incidentId(), e.getMessage());
        }
    }
}
