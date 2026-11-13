package tn.esprit.forestfirewatch.ressources.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forestfirewatch.ressources.entity.OutboxEvent;
import tn.esprit.forestfirewatch.ressources.repository.OutboxEventRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Écrit un événement dans la table outbox, dans la transaction en cours
 * (MANDATORY : impossible de l'appeler hors transaction).
 * La publication réelle dans RabbitMQ est faite par OutboxPublisher.
 */
@Service
public class OutboxService {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent enregistrer(String type, Object evenement) {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("eventId", eventId);
        corps.put("type", type);
        corps.putAll(objectMapper.convertValue(evenement, new TypeReference<Map<String, Object>>() { }));
        try {
            return outboxRepository.save(new OutboxEvent(eventId, type, objectMapper.writeValueAsString(corps)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Sérialisation impossible de l'événement " + type, e);
        }
    }
}
