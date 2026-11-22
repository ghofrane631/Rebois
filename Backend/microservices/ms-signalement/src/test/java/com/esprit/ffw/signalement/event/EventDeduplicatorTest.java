package com.esprit.ffw.signalement.event;

import com.esprit.ffw.signalement.event.ProcessedEvent;
import com.esprit.ffw.signalement.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventDeduplicatorTest {

    @Mock ProcessedEventRepository repository;
    @InjectMocks EventDeduplicator deduplicator;

    @Test
    void traiteUnEvenementPourLaPremiereFois() {
        UUID eventId = UUID.randomUUID();
        when(repository.insert(any(ProcessedEvent.class))).thenAnswer(i -> i.getArgument(0));
        AtomicInteger executions = new AtomicInteger();

        deduplicator.executerUneFois(eventId, "test", executions::incrementAndGet);

        assertEquals(1, executions.get());
        verify(repository, never()).deleteById(any());
    }

    @Test
    void ignoreUnEvenementDejaTraite() {
        UUID eventId = UUID.randomUUID();
        when(repository.insert(any(ProcessedEvent.class))).thenThrow(new DuplicateKeyException("déjà traité"));
        AtomicInteger executions = new AtomicInteger();

        deduplicator.executerUneFois(eventId, "test", executions::incrementAndGet);

        assertEquals(0, executions.get());
    }

    @Test
    void supprimeLeMarqueurSiLeTraitementEchoue() {
        UUID eventId = UUID.randomUUID();
        when(repository.insert(any(ProcessedEvent.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(IllegalStateException.class, () -> deduplicator.executerUneFois(eventId, "test", () -> {
            throw new IllegalStateException("boom");
        }));

        verify(repository).deleteById(eventId.toString());
    }

    @Test
    void traiteSansDeduplicationSiEventIdAbsent() {
        AtomicInteger executions = new AtomicInteger();

        deduplicator.executerUneFois(null, "test", executions::incrementAndGet);

        assertEquals(1, executions.get());
        verifyNoInteractions(repository);
    }
}
