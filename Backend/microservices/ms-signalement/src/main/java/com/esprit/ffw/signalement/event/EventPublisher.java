package com.esprit.ffw.signalement.event;

import com.esprit.ffw.signalement.config.RabbitConfig;
import com.esprit.ffw.signalement.model.Signalement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publierFeuConfirme(Signalement s) {
        FeuConfirmeEvent event = new FeuConfirmeEvent(UUID.randomUUID(), s.getId(), s.getSecteurId(),
                s.getPosition().getY(), s.getPosition().getX(), s.getIntensite(), Instant.now());
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.KEY_FEU_CONFIRME, event);
            log.info("FeuConfirme publié eventId={} signalementId={}", event.eventId(), s.getId());
        } catch (Exception e) {
            // S7 : remplacé par l'outbox (écriture dans la même transaction + republication)
            log.error("Échec publication FeuConfirme signalementId={}", s.getId(), e);
        }
    }
}
