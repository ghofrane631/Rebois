package com.esprit.ffw.signalement.event;

import com.esprit.ffw.signalement.config.RabbitConfig;
import com.esprit.ffw.signalement.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock OutboxRepository outbox;
    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks OutboxPublisher publisher;

    private OutboxMessage message() {
        OutboxMessage m = new OutboxMessage();
        m.setEventId("evt-1");
        m.setType("FeuConfirme");
        m.setRoutingKey(RabbitConfig.KEY_FEU_CONFIRME);
        m.setPayload("{\"eventId\":\"evt-1\"}");
        m.setStatut(OutboxStatus.PENDING);
        m.setCreeLe(Instant.now());
        m.setNextAttemptAt(Instant.now());
        return m;
    }

    @Test
    void publieEtMarqueEnvoye() {
        OutboxMessage m = message();
        when(outbox.findTop50ByStatutAndNextAttemptAtLessThanEqualOrderByCreeLeAsc(eq(OutboxStatus.PENDING), any()))
                .thenReturn(List.of(m));

        publisher.publierEnAttente();

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(rabbitTemplate).send(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.KEY_FEU_CONFIRME), captor.capture());
        assertEquals("evt-1", captor.getValue().getMessageProperties().getHeader("eventId"));
        assertEquals(OutboxStatus.SENT, m.getStatut());
        assertNotNull(m.getEnvoyeLe());
        verify(outbox).save(m);
    }

    @Test
    void enCasDEchecProgrammeUnRetryAvecBackoff() {
        OutboxMessage m = message();
        when(outbox.findTop50ByStatutAndNextAttemptAtLessThanEqualOrderByCreeLeAsc(eq(OutboxStatus.PENDING), any()))
                .thenReturn(List.of(m));
        doThrow(new AmqpException("rabbit down"))
                .when(rabbitTemplate).send(any(), any(), any(Message.class));

        publisher.publierEnAttente();

        assertEquals(OutboxStatus.PENDING, m.getStatut());
        assertEquals(1, m.getTentatives());
        assertNotNull(m.getDerniereErreur());
        assertTrue(m.getNextAttemptAt().isAfter(Instant.now()));
        verify(outbox).save(m);
    }

    @Test
    void abandonneApresLeNombreMaximumDeTentatives() {
        OutboxMessage m = message();
        m.setTentatives(OutboxPublisher.MAX_TENTATIVES - 1);
        when(outbox.findTop50ByStatutAndNextAttemptAtLessThanEqualOrderByCreeLeAsc(eq(OutboxStatus.PENDING), any()))
                .thenReturn(List.of(m));
        doThrow(new AmqpException("rabbit down"))
                .when(rabbitTemplate).send(any(), any(), any(Message.class));

        publisher.publierEnAttente();

        assertEquals(OutboxStatus.FAILED, m.getStatut());
        assertEquals(OutboxPublisher.MAX_TENTATIVES, m.getTentatives());
    }

    @Test
    void leBackoffEstBorne() {
        assertEquals(5, OutboxPublisher.backoff(1));
        assertEquals(20, OutboxPublisher.backoff(3));
        assertEquals(OutboxPublisher.BACKOFF_MAX_SECONDES, OutboxPublisher.backoff(20));
    }
}
