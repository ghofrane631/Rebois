package com.esprit.ffw.signalement.event;

import com.esprit.ffw.signalement.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Garantit qu'un eventId n'est traité qu'une fois. Le marqueur est inséré avant le traitement
 * (insert échoue si l'eventId existe déjà) puis supprimé si le traitement échoue,
 * pour laisser RabbitMQ réessayer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventDeduplicator {

    private final ProcessedEventRepository repository;

    public void executerUneFois(UUID eventId, String consommateur, Runnable traitement) {
        if (eventId == null) {
            log.warn("Événement sans eventId : déduplication impossible, traitement direct");
            traitement.run();
            return;
        }
        String id = eventId.toString();
        try {
            repository.insert(new ProcessedEvent(id, consommateur, Instant.now()));
        } catch (DuplicateKeyException e) {
            log.info("Événement {} déjà traité par {} : ignoré", id, consommateur);
            return;
        }
        try {
            traitement.run();
        } catch (RuntimeException e) {
            repository.deleteById(id);
            throw e;
        }
    }
}
