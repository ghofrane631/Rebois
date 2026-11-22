package tn.reboistn.restauration.event;

import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.reboistn.restauration.domain.EvenementOutbox;
import tn.reboistn.restauration.repository.EvenementOutboxRepository;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxDispatcher {

    private static final System.Logger LOGGER = System.getLogger(OutboxDispatcher.class.getName());

    private final EvenementOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxDispatcher(EvenementOutboxRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${app.outbox.delay:5000}")
    @Transactional
    public void publishPendingEvents() {
        for (EvenementOutbox event : outboxRepository.findTop100ByPublieLeIsNullOrderByCreeLeAsc()) {
            try {
                rabbitTemplate.invoke(operations -> {
                    CorrelationData correlation = new CorrelationData(event.getEventId());
                    operations.send(event.getExchangeName(), event.getRoutingKey(), MessageBuilder
                            .withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                            .setMessageId(event.getEventId())
                            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                            .build(), correlation);
                    try {
                        CorrelationData.Confirm confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
                        if (!confirm.isAck() || correlation.getReturned() != null) {
                            throw new IllegalStateException("RabbitMQ n'a pas confirme le routage de l'evenement");
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Attente de confirmation RabbitMQ interrompue", exception);
                    } catch (Exception exception) {
                        throw new IllegalStateException("Confirmation RabbitMQ indisponible", exception);
                    }
                    return null;
                });
                event.markPublished();
            } catch (RuntimeException exception) {
                LOGGER.log(System.Logger.Level.WARNING,
                    "Echec de publication de l'evenement {0}: {1}", event.getEventId(), exception.getMessage());
            }
        }
    }
}