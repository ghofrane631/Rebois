package tn.reboistn.restauration.event;

import java.math.BigDecimal;

public record InterventionTermineeEvent(String eventId, Long incidentId, Long secteurId,
                                        BigDecimal surfaceBruleeHa, String gouvernorat) {
}