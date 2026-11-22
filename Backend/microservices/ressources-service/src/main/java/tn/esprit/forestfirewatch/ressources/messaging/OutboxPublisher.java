package tn.esprit.forestfirewatch.ressources.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forestfirewatch.ressources.entity.OutboxEvent;
import tn.esprit.forestfirewatch.ressources.repository.OutboxEventRepository;

import java.nio.charset.StandardCharsets;

/**
 * Publie dans RabbitMQ les événements de l'outbox pas encore envoyés.
 * Si RabbitMQ est en panne, rien n'est perdu : on réessaie au passage suivant.
 */
@Component
@ConditionalOnProperty(name = "ressources.outbox.publisher-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxEventRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${ressources.outbox.delai-ms:2000}")
    @Transactional
    public void publier() {
        for (OutboxEvent e : outboxRepository.findTop50ByPublieFalseOrderByCreeLeAsc()) {
            Message message = MessageBuilder.withBody(e.getPayload().getBytes(StandardCharsets.UTF_8))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setMessageId(e.getEventId())
                    .setHeader("eventType", e.getType())
                    .build();
            try {
                rabbitTemplate.send(RabbitConfig.EXCHANGE, e.getType(), message);
                e.marquerPublie();
                log.info("Événement publié : {} ({})", e.getType(), e.getEventId());
            } catch (AmqpException ex) {
                log.warn("RabbitMQ indisponible, {} restera dans l'outbox : {}", e.getEventId(), ex.getMessage());
                return;
            }
        }
    }
}
