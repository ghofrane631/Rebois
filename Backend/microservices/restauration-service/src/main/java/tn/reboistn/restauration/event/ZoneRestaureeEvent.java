package tn.reboistn.restauration.event;

import java.time.LocalDateTime;

public record ZoneRestaureeEvent(String eventId, Long zoneId, Long secteurId,
                                 int couverturePct, LocalDateTime publieLe) {
}