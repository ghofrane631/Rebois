package com.esprit.ffw.signalement.event;

import com.esprit.ffw.signalement.config.RabbitConfig;
import com.esprit.ffw.signalement.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Publie les événements en attente avec backoff exponentiel ; abandonne après 10 essais. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    static final int MAX_TENTATIVES = 10;
    static final int BACKOFF_BASE_SECONDES = 5;
    static final int BACKOFF_MAX_SECONDES = 300;
    static final int RETENTION_JOURS = 7;

    private final OutboxRepository outbox;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.publisher-delay-ms:5000}")
    public void publierEnAttente() {
        List<OutboxMessage> messages = outbox
                .findTop50ByStatutAndNextAttemptAtLessThanEqualOrderByCreeLeAsc(OutboxStatus.PENDING, Instant.now());
        for (OutboxMessage message : messages) {
            try {
                publier(message);
            } catch (Exception e) {
                log.error("Erreur inattendue lors du traitement de l'outbox {}", message.getEventId(), e);
            }
        }
    }

    private void publier(OutboxMessage message) {
        try {
            Message amqpMessage = MessageBuilder
                    .withBody(message.getPayload().getBytes(StandardCharsets.UTF_8))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setMessageId(message.getEventId())
                    .setHeader("eventId", message.getEventId())
                    .build();
            rabbitTemplate.send(RabbitConfig.EXCHANGE, message.getRoutingKey(), amqpMessage);

            message.setStatut(OutboxStatus.SENT);
            message.setEnvoyeLe(Instant.now());
            message.setDerniereErreur(null);
            outbox.save(message);
            log.info("Événement {} publié sur {}", message.getEventId(), message.getRoutingKey());
        } catch (Exception e) {
            message.setTentatives(message.getTentatives() + 1);
            message.setDerniereErreur(tronquer(e.toString()));
            if (message.getTentatives() >= MAX_TENTATIVES) {
                message.setStatut(OutboxStatus.FAILED);
                log.error("Événement {} abandonné après {} tentatives", message.getEventId(), MAX_TENTATIVES, e);
            } else {
                message.setNextAttemptAt(Instant.now().plus(backoff(message.getTentatives()), ChronoUnit.SECONDS));
                log.warn("Publication de {} échouée (tentative {}) : {}",
                        message.getEventId(), message.getTentatives(), e.toString());
            }
            outbox.save(message);
        }
    }

    static long backoff(int tentatives) {
        long delay = BACKOFF_BASE_SECONDES * (1L << Math.min(tentatives - 1, 10));
        return Math.min(delay, BACKOFF_MAX_SECONDES);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void purgerEnvoyes() {
        long supprimes = outbox.deleteByStatutAndEnvoyeLeBefore(OutboxStatus.SENT,
                Instant.now().minus(RETENTION_JOURS, ChronoUnit.DAYS));
        if (supprimes > 0) {
            log.info("Purge outbox : {} messages envoyés supprimés", supprimes);
        }
    }

    private String tronquer(String valeur) {
        if (valeur == null) return null;
        return valeur.length() <= 500 ? valeur : valeur.substring(0, 500);
    }
}
