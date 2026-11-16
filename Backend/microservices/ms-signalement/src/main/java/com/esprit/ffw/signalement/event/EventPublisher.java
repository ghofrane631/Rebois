package com.esprit.ffw.signalement.event;

import com.esprit.ffw.signalement.config.RabbitConfig;
import com.esprit.ffw.signalement.model.Signalement;
import com.esprit.ffw.signalement.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Écrit l'événement dans l'outbox au lieu de publier directement :
 * l'API ne dépend plus de la disponibilité de RabbitMQ, et un publieur planifié réessaie.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final OutboxRepository outbox;
    private final ObjectMapper mapper;

    public void publierFeuConfirme(Signalement s) {
        FeuConfirmeEvent event = new FeuConfirmeEvent(UUID.randomUUID(), s.getId(), s.getSecteurId(),
                s.getPosition().getY(), s.getPosition().getX(), s.getIntensite(), Instant.now());
        enregistrer(event.eventId().toString(), "FeuConfirme", RabbitConfig.KEY_FEU_CONFIRME, event);
    }

    private void enregistrer(String eventId, String type, String routingKey, Object event) {
        try {
            OutboxMessage message = new OutboxMessage();
            message.setEventId(eventId);
            message.setType(type);
            message.setRoutingKey(routingKey);
            message.setPayload(mapper.writeValueAsString(event));
            message.setStatut(OutboxStatus.PENDING);
            message.setCreeLe(Instant.now());
            message.setNextAttemptAt(Instant.now());
            outbox.save(message);
            log.info("Événement {} mis en outbox (type={})", eventId, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Sérialisation impossible de l'événement " + type, e);
        }
    }
}
