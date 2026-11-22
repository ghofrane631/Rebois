package tn.reboistn.restauration.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tn.reboistn.restauration.domain.EvenementOutbox;
import tn.reboistn.restauration.repository.EvenementOutboxRepository;

@Component
public class ZoneEventPublisher {

    private final ObjectMapper objectMapper;
    private final EvenementOutboxRepository outboxRepository;
    private final String exchange;
    private final String zoneRestaureeRoutingKey;

    public ZoneEventPublisher(ObjectMapper objectMapper,
                              EvenementOutboxRepository outboxRepository,
                              @Value("${app.events.exchange}") String exchange,
                              @Value("${app.events.zone-restauree-routing-key}") String zoneRestaureeRoutingKey) {
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
        this.exchange = exchange;
        this.zoneRestaureeRoutingKey = zoneRestaureeRoutingKey;
    }

    public void publish(ZoneRestaureeEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(new EvenementOutbox(event.eventId(), "ZoneRestauree", exchange,
                    zoneRestaureeRoutingKey, payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Impossible de serialiser l'evenement ZoneRestauree", exception);
        }
    }
}